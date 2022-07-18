package fr.skytasul.quests.expansion.tracking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.stages.types.Locatable;

public abstract class AbstractTaskShownTracker extends AbstractTaskTracker {
	
	protected List<Player> shown;

	protected AbstractTaskShownTracker(long period) {
		super(period);
	}

	@Override
	public void start(Locatable locatable) {
		super.start(locatable);
		if (locatable.canBeFetchedAsynchronously()) {
			shown = new CopyOnWriteArrayList<>();
		}else {
			shown = new ArrayList<>();
		}
	}

	@Override
	public void show(Player player) {
		shown.add(player);
	}

	@Override
	public void hide(Player player) {
		shown.remove(player);
	}
	
}