package fr.skytasul.quests.expansion.api.tracking;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectsRegistry;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.serializable.SerializableCreator;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.expansion.tracking.BlockOutlineTracker;
import fr.skytasul.quests.expansion.tracking.ParticleTracker;
import fr.skytasul.quests.expansion.tracking.TrackingOption;
import fr.skytasul.quests.expansion.utils.LangExpansion;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.nms.NMS;

public class TrackerRegistry extends QuestObjectsRegistry<Tracker, TrackerCreator> {
	
	public TrackerRegistry() {
		super("trackers", "Location trackers");
		
		registerTrackers();
		registerOption();
	}
	
	private void registerTrackers() {
		if (NMS.getMCVersion() > 8)
			register(new TrackerCreator("particles", ParticleTracker.class, ItemUtils.item(XMaterial.SPLASH_POTION, LangExpansion.Tracking_Particles_Name.toString(), QuestOption.formatDescription(LangExpansion.Tracking_Particles_Description.toString())), ParticleTracker::new));
		if (NMS.getMCVersion() >= 17)
			register(new TrackerCreator("block-outline", BlockOutlineTracker.class, ItemUtils.item(XMaterial.ACACIA_STAIRS, LangExpansion.Tracking_Outline_Name.toString(), QuestOption.formatDescription(LangExpansion.Tracking_Outline_Description.toString())), BlockOutlineTracker::new));
	}

	private void registerOption() {
		QuestsAPI.getStages().getTypes()
			.stream()
			.filter(type -> Locatable.class.isAssignableFrom(type.getStageClass()))
			.map(StageType.class::cast)
			.forEach(type -> type.getOptionsRegistry().register(new SerializableCreator<>("expansion-tracking", TrackingOption.class, () -> new TrackingOption(type.getStageClass()))));
	}
	
}
