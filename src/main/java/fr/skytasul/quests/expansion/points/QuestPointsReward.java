package fr.skytasul.quests.expansion.points;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.expansion.BeautyQuestsExpansion;
import fr.skytasul.quests.expansion.utils.LangExpansion;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.Lang;

public class QuestPointsReward extends AbstractReward {
	
	private int min;
	private int max;
	
	public QuestPointsReward() {}
	
	public QuestPointsReward(String customDescription, int min, int max) {
		super(customDescription);
		this.min = min;
		this.max = max;
	}
	
	@Override
	public List<String> give(Player p) {
		try {
			int points = min == max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
			BeautyQuestsExpansion.getInstance().getPointsManager().addPoints(PlayersManager.getPlayerAccount(p), points);
			return Arrays.asList(LangExpansion.Points_Value.format(points));
		} catch (IllegalPointsBalanceException ex) {
			throw new IllegalArgumentException(ex);
		}
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return LangExpansion.Points_Value.format(min == max ? min : (min + " - " + max));
	}
	
	@Override
	public AbstractReward clone() {
		return new QuestPointsReward(getCustomDescription(), min, max);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { QuestOption.formatNullableValue(getDescription(null)), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		LangExpansion.Points_Reward_Editor_Min.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::cancel, newMin -> {
			LangExpansion.Points_Reward_Editor_Max.send(event.getPlayer());
			new TextEditor<>(event.getPlayer(), event::cancel, newMax -> {
				min = newMin;
				max = newMax;
				
				event.reopenGUI();
			}, NumberParser.INTEGER_PARSER_POSITIVE).enter();
		}, NumberParser.INTEGER_PARSER_POSITIVE).enter();
	}
	
	@Override
	public void save(ConfigurationSection section) {
		section.set("min", min);
		section.set("max", max);
	}
	
	@Override
	public void load(ConfigurationSection section) {
		min = section.getInt("min");
		max = section.getInt("max");
	}
	
}
