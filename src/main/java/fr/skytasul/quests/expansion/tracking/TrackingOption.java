package fr.skytasul.quests.expansion.tracking;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.api.stages.options.StageOption;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.expansion.BeautyQuestsExpansion;
import fr.skytasul.quests.expansion.api.tracking.Tracker;
import fr.skytasul.quests.expansion.api.tracking.TrackerCreator;
import fr.skytasul.quests.expansion.utils.LangExpansion;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.utils.XMaterial;

public class TrackingOption extends StageOption<AbstractStage> {
	
	private List<Tracker> trackers;
	
	private int itemSlot;
	
	public TrackingOption(Class<AbstractStage> stageClass) {
		super(stageClass);
	}
	
	@Override
	public StageOption<AbstractStage> clone() {
		TrackingOption option = new TrackingOption(getStageClass());
		if (trackers != null && !trackers.isEmpty()) option.trackers = trackers.stream().map(Tracker::clone).collect(Collectors.toList());
		return option;
	}
	
	@Override
	public boolean shouldSave() {
		return trackers != null && !trackers.isEmpty();
	}
	
	@Override
	public void save(ConfigurationSection section) {
		for (Tracker tracker : trackers) {
			tracker.save(section.createSection(tracker.getCreator().getID()));
		}
	}
	
	@Override
	public void load(ConfigurationSection section) {
		for (String key : section.getKeys(false)) {
			TrackerCreator creator = BeautyQuestsExpansion.getInstance().getTrackersRegistry().getByID(key);
			if (creator != null) {
				if (trackers == null) trackers = new ArrayList<>();
				Tracker tracker = creator.newObject();
				tracker.load(section.getConfigurationSection(key));
				trackers.add(tracker);
			}
		}
	}
	
	@Override
	public void startEdition(StageCreation<AbstractStage> creation) {
		itemSlot = creation.getLine().setItem(10, ItemUtils.item(XMaterial.COMPASS, LangExpansion.Tracking_Gui_Name.toString(), getLore()), (p, item) -> {
			BeautyQuestsExpansion.getInstance().getTrackersRegistry().createGUI(QuestObjectLocation.OTHER, trackers -> {
				this.trackers = trackers.isEmpty() ? null : trackers;
				creation.getLine().editItem(itemSlot, ItemUtils.lore(creation.getLine().getItem(itemSlot), getLore()));
				creation.reopenGUI(p, true);
			}, trackers == null ? new ArrayList<>() : trackers).create(p);
		});
	}
	
	private String[] getLore() {
		return new String[] { QuestOption.formatDescription(LangExpansion.Tracking_Trackers.format(trackers == null ? 0 : trackers.size())) };
	}
	
	@Override
	public void stageLoad(AbstractStage stage) {
		if (trackers != null) trackers.forEach(x -> x.start((Locatable) stage));
	}
	
	@Override
	public void stageUnload(AbstractStage stage) {
		if (trackers != null) trackers.forEach(Tracker::stop);
	}
	
	@Override
	public void stageStart(PlayerAccount acc, AbstractStage stage) {
		if (acc.isCurrent()) showTrackers(acc.getPlayer(), (Locatable) stage);
	}
	
	@Override
	public void stageJoin(PlayerAccount acc, Player p, AbstractStage stage) {
		showTrackers(p, (Locatable) stage);
	}
	
	@Override
	public void stageEnd(PlayerAccount acc, AbstractStage stage) {
		if (acc.isCurrent()) hideTrackers(acc.getPlayer());
	}
	
	@Override
	public void stageLeave(PlayerAccount acc, Player p, AbstractStage stage) {
		hideTrackers(p);
	}
	
	private void showTrackers(Player player, Locatable stage) {
		if (trackers != null && stage.isShown(player)) trackers.forEach(x -> x.show(player));
	}
	
	private void hideTrackers(Player p) {
		if (trackers != null) trackers.forEach(x -> x.hide(p));
	}
	
}
