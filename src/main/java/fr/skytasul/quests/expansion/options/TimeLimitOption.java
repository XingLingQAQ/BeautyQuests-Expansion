package fr.skytasul.quests.expansion.options;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.DurationParser.MinecraftTimeUnit;
import fr.skytasul.quests.api.events.PlayerQuestResetEvent;
import fr.skytasul.quests.api.events.QuestFinishEvent;
import fr.skytasul.quests.api.events.QuestLaunchEvent;
import fr.skytasul.quests.api.events.accounts.PlayerAccountJoinEvent;
import fr.skytasul.quests.api.events.accounts.PlayerAccountLeaveEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayerQuestDatas;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.expansion.BeautyQuestsExpansion;
import fr.skytasul.quests.expansion.utils.LangExpansion;
import fr.skytasul.quests.utils.QuestUtils;

public class TimeLimitOption extends QuestOption<Integer> implements Listener, QuestDescriptionProvider {

	private Map<PlayerAccount, BukkitTask> tasks;

	@Override
	public Object save() {
		return getValue();
	}

	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(config.getInt(key));
	}

	@Override
	public Integer cloneValue(Integer value) {
		return value;
	}

	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.item(XMaterial.CLOCK, "§d" + LangExpansion.TimeLimit_Name.toString(), getLore());
	}

	private String[] getLore() {
		return new String[] { formatDescription(LangExpansion.TimeLimit_Description.toString()), "", formatValue(Utils.millisToHumanString(getValue() * 1000L)), "", LangExpansion.Expansion_Label.toString() };
	}

	@Override
	public void click(@NotNull QuestCreationGuiClickEvent event) {
		LangExpansion.TimeLimit_EDITOR.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::reopen, obj -> {
			setValue(obj.intValue());
			ItemUtils.lore(event.getClicked(), getLore());
			event.reopen();
		}, () -> {
			resetValue();
			ItemUtils.lore(event.getClicked(), getLore());
			event.reopen();
		}, MinecraftTimeUnit.SECOND.getParser()).start();
	}

	@Override
	public void attach(Quest quest) {
		super.attach(quest);

		tasks = new HashMap<>();
	}

	@Override
	public void detach() {
		super.detach();

		if (tasks != null) {
			tasks.forEach((acc, task) -> task.cancel());
			tasks = null;
		}
	}

	private void startTask(PlayerAccount account) {
		PlayerQuestDatas datas = account.getQuestDatasIfPresent(getAttachedQuest());
		if (datas == null) {
			BeautyQuestsExpansion.logger.warning(
					"Cannot find player datas of " + account.debugName() + " for quest " + getAttachedQuest().getId());
			return;
		}
		long startingTime = datas.getStartingTime();
		if (startingTime == 0) return; // outdated datas

		long timeToWait = startingTime + getValue() * 1000 - System.currentTimeMillis();
		if (timeToWait <= 0) {
			QuestUtils.runSync(() -> getAttachedQuest().cancelPlayer(account));
		}else {
			tasks.put(account, Bukkit.getScheduler().runTaskLater(BeautyQuestsExpansion.getInstance(), () -> getAttachedQuest().cancelPlayer(account), timeToWait / 50));
		}
	}

	private void cancelTask(PlayerAccount account) {
		BukkitTask task = tasks.remove(account);
		if (task != null) task.cancel();
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onAccountJoin(PlayerAccountJoinEvent event) {
		if (getAttachedQuest().hasStarted(event.getPlayerAccount())) startTask(event.getPlayerAccount());
	}

	@EventHandler
	public void onQuestStart(QuestLaunchEvent event) {
		if (event.getQuest() == getAttachedQuest()) startTask(event.getPlayerAccount());
	}

	@EventHandler
	public void onAccountLeave(PlayerAccountLeaveEvent event) {
		cancelTask(event.getPlayerAccount());
	}

	@EventHandler
	public void onQuestFinish(QuestFinishEvent event) {
		if (event.getQuest() == getAttachedQuest()) cancelTask(event.getPlayerAccount());
	}

	@EventHandler
	public void onQuestCancel(PlayerQuestResetEvent event) {
		if (event.getQuest() == getAttachedQuest()) cancelTask(event.getPlayerAccount());
	}

	@Override
	public List<String> provideDescription(QuestDescriptionContext context) {
		if (!context.getPlayerAccount().isCurrent()) return null;
		if (context.getCategory() != PlayerListCategory.IN_PROGRESS)
			return null;

		PlayerQuestDatas datas = context.getPlayerAccount().getQuestDatasIfPresent(getAttachedQuest());
		if (datas == null) {
			BeautyQuestsExpansion.logger.warning("Cannot find player datas of " + context.getPlayerAccount().debugName()
					+ " for quest " + getAttachedQuest().getId());
			return null;
		}
		long startingTime = datas.getStartingTime();
		if (startingTime == 0) return null; // outdated datas

		long timeToWait = startingTime + getValue() * 1000 - System.currentTimeMillis();
		return Arrays.asList(LangExpansion.TimeLimit_Left.quickFormat("time_left", Utils.millisToHumanString(timeToWait)));
	}

	@Override
	public String getDescriptionId() {
		return "time_left";
	}

	@Override
	public double getDescriptionPriority() {
		return 50;
	}

}
