package fr.skytasul.quests.expansion.tracking;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.Located;
import fr.skytasul.quests.api.stages.types.Locatable.MultipleLocatable;
import fr.skytasul.quests.api.stages.types.Locatable.MultipleLocatable.NearbyFetcher;
import fr.skytasul.quests.api.stages.types.Locatable.PreciseLocatable;

public abstract class AbstractTaskFetcherTracker extends AbstractTaskShownTracker {
	
	protected AbstractTaskFetcherTracker(long period) {
		super(period);
	}
	
	@Override
	public void run() {
		if (shown.isEmpty()) return;
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
				Spliterator<Located> nearbyLocated = multiple.getNearbyLocated(constructFetcher(player));
				if (nearbyLocated == null) continue;
				Collection<Located> playerLocated =
						StreamSupport.stream(nearbyLocated, false)
							.limit(getAmount(player))
							.collect(Collectors.toList());
				
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
	
	protected abstract int getAmount(Player player);
	
}