package fr.skytasul.quests.expansion.api.tracking;

import java.util.function.Predicate;
import java.util.function.Supplier;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.api.stages.StageType;

@SuppressWarnings("rawtypes")
public class TrackerCreator extends QuestObjectCreator<Tracker> {

	private Predicate<StageType> stageFilter;

	public TrackerCreator(String id, Class<? extends Tracker> clazz, ItemStack item, Supplier<Tracker> newObjectSupplier) {
		this(id, clazz, item, newObjectSupplier, null);
	}

	public TrackerCreator(String id, Class<? extends Tracker> clazz, ItemStack item, Supplier<Tracker> newObjectSupplier,
			Predicate<StageType> stageFilter) {
		super(id, clazz, item, newObjectSupplier, false);
		this.stageFilter = stageFilter;
	}

	public boolean matches(StageType type) {
		return stageFilter == null || stageFilter.test(type);
	}

}
