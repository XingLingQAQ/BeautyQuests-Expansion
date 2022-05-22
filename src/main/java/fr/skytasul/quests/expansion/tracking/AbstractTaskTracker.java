package fr.skytasul.quests.expansion.tracking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.Located;
import fr.skytasul.quests.api.stages.types.Locatable.MultipleLocatable;
import fr.skytasul.quests.api.stages.types.Locatable.MultipleLocatable.NearbyFetcher;
import fr.skytasul.quests.api.stages.types.Locatable.PreciseLocatable;
import fr.skytasul.quests.expansion.BeautyQuestsExpansion;
import fr.skytasul.quests.expansion.api.tracking.Tracker;

public abstract class AbstractTaskTracker extends Tracker {
	
	private BukkitTask task;
	
	protected Locatable locatable;
	protected List<Player> shown;

	@Override
	public void start(Locatable locatable) {
		this.locatable = locatable;
		if (locatable.canBeFetchedAsynchronously()) {
			task = Bukkit.getScheduler().runTaskTimerAsynchronously(BeautyQuestsExpansion.getInstance(), this::run, 20L, 20L);
			shown = new CopyOnWriteArrayList<>();
		}else {
			task = Bukkit.getScheduler().runTaskTimer(BeautyQuestsExpansion.getInstance(), this::run, 20L, 20L);
			shown = new ArrayList<>();
		}
	}

	@Override
	public void stop() {
		if (task != null) task.cancel();
		shown = null;
	}
	
	@Override
	public void show(Player player) {
		shown.add(player);
	}
	
	@Override
	public void hide(Player player) {
		shown.remove(player);
	}

	private void run() {
		if (locatable instanceof Locatable.PreciseLocatable) {
			Locatable.PreciseLocatable precise = (PreciseLocatable) locatable;
			Located located = precise.getLocated();
			if (located != null) display(located);
		}
		
		if (locatable instanceof Locatable.MultipleLocatable) {
			Locatable.MultipleLocatable multiple = (MultipleLocatable) locatable;
			Set<Locatable.Located> located = new HashSet<>();
			for (Player player : shown) {
				Collection<Located> playerLocated = multiple.getNearbyLocated(constructFetcher(player));
				if (playerLocated == null) break;
				located.addAll(playerLocated);
			}
			located.forEach(this::display);
		}
	}
	
	private void display(Locatable.Located located) {
		if (located instanceof Locatable.Located.LocatedBlock) {
			displayBlock(((Locatable.Located.LocatedBlock) located).getBlock());
		}else if (located instanceof Locatable.Located.LocatedEntity) {
			displayEntity(((Locatable.Located.LocatedEntity) located).getEntity());
		}else {
			displayLocation(located.getLocation());
		}
	}

	protected void displayBlock(Block block) {
		displayLocation(block.getLocation());
	}

	protected void displayEntity(Entity entity) {
		displayLocation(entity.getLocation());
	}

	protected abstract void displayLocation(Location location);
	
	protected abstract NearbyFetcher constructFetcher(Player player);
	
}