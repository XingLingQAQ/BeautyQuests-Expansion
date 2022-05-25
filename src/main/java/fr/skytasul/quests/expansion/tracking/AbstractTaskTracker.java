package fr.skytasul.quests.expansion.tracking;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.expansion.BeautyQuestsExpansion;
import fr.skytasul.quests.expansion.api.tracking.Tracker;

public abstract class AbstractTaskTracker extends Tracker implements Runnable {
	
	private BukkitTask task;
	private long period;
	
	protected AbstractTaskTracker(long period) {
		this.period = period;
	}
	
	protected long getDelay() {
		return period;
	}
	
	@Override
	public void start(Locatable locatable) {
		if (locatable.canBeFetchedAsynchronously()) {
			task = Bukkit.getScheduler().runTaskTimerAsynchronously(BeautyQuestsExpansion.getInstance(), this, getDelay(), period);
		}else {
			task = Bukkit.getScheduler().runTaskTimer(BeautyQuestsExpansion.getInstance(), this, getDelay(), period);
		}
	}

	@Override
	public void stop() {
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

	public boolean isRunning() {
		return task != null;
	}
	
}