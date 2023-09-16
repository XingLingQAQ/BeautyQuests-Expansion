package fr.skytasul.quests.expansion.options;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.options.StageOption;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.expansion.BeautyQuestsExpansion;
import fr.skytasul.quests.expansion.api.tracking.Tracker;
import fr.skytasul.quests.expansion.api.tracking.TrackerCreator;
import fr.skytasul.quests.expansion.utils.LangExpansion;

public class TrackingOption<T extends AbstractStage & Locatable> extends StageOption<T> {

	private List<Tracker> trackers;

	private int itemSlot;

	public TrackingOption(Class<T> stageClass) {
		super(stageClass);
	}

	@Override
	public StageOption<T> clone() {
		TrackingOption<T> option = new TrackingOption<>(getStageClass());
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
	public void startEdition(StageCreation<T> creation) {
		itemSlot = creation.getLine().setItem(10,
				ItemUtils.item(XMaterial.COMPASS, LangExpansion.Tracking_Gui_Name.toString(), getLore()), event -> {
					BeautyQuestsExpansion.getInstance().getTrackersRegistry()
							.createGUI(QuestObjectLocation.OTHER, trackers -> {
								this.trackers = trackers.isEmpty() ? null : trackers;
								creation.getLine().refreshItemLore(itemSlot, getLore());
								event.reopen();
							}, trackers == null ? new ArrayList<>() : trackers,
									creator -> creator.matches(creation.getCreationContext().getType()))
							.open(event.getPlayer());
				});
	}

	private String[] getLore() {
		return new String[] {QuestOption.formatDescription(
				LangExpansion.Tracking_Trackers.quickFormat("trackers_amount", trackers == null ? 0 : trackers.size())), "",
				LangExpansion.Expansion_Label.toString()};
	}

	@Override
	public void stageLoad(StageController stage) {
		if (trackers != null)
			trackers.forEach(x -> x.start((T) stage.getStage()));
	}

	@Override
	public void stageUnload(StageController stage) {
		if (trackers != null) trackers.forEach(Tracker::stop);
	}

	@Override
	public void stageStart(PlayerAccount acc, StageController stage) {
		if (acc.isCurrent())
			showTrackers(acc.getPlayer(), (T) stage.getStage());
	}

	@Override
	public void stageJoin(Player p, StageController stage) {
		showTrackers(p, (T) stage.getStage());
	}

	@Override
	public void stageEnd(PlayerAccount acc, StageController stage) {
		if (acc.isCurrent()) hideTrackers(acc.getPlayer());
	}

	@Override
	public void stageLeave(Player p, StageController stage) {
		hideTrackers(p);
	}

	private void showTrackers(Player player, Locatable stage) {
		if (trackers != null && stage.isShown(player)) trackers.forEach(x -> x.show(player));
	}

	private void hideTrackers(Player p) {
		if (trackers != null) trackers.forEach(x -> x.hide(p));
	}

}
