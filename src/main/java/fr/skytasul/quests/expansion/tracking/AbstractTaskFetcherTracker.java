package fr.skytasul.quests.expansion.tracking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.Located;
import fr.skytasul.quests.api.stages.types.Locatable.MultipleLocatable;
import fr.skytasul.quests.api.stages.types.Locatable.MultipleLocatable.NearbyFetcher;
import fr.skytasul.quests.api.stages.types.Locatable.PreciseLocatable;

public abstract class AbstractTaskFetcherTracker extends AbstractTaskTracker {
	
	protected Locatable locatable;
	protected List<Player> shown;
	
	protected AbstractTaskFetcherTracker(long period) {
		super(period);
	}
	
	@Override
	public void start(Locatable locatable) {
		super.start(locatable);
		this.locatable = locatable;
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
	
	@Override
	public void run() {
		if (locatable instanceof Locatable.PreciseLocatable) {
			Locatable.PreciseLocatable precise = (PreciseLocatable) locatable;
			Located located = precise.getLocated();
			if (!isRunning()) return;
			if (located != null) display(located);
		}
		
		if (locatable instanceof Locatable.MultipleLocatable) {
			Locatable.MultipleLocatable multiple = (MultipleLocatable) locatable;
			Set<Locatable.Located> located = new HashSet<>();
			for (Player player : shown) {
				Collection<Located> playerLocated = multiple.getNearbyLocated(constructFetcher(player));
				if (!isRunning()) return;
				if (playerLocated == null) break;
				located.addAll(playerLocated);
			}
			if (!isRunning()) return;
			located.forEach(this::display);
		}
	}
	
	protected abstract void display(Locatable.Located located);
	
	protected abstract NearbyFetcher constructFetcher(Player player);
	
}