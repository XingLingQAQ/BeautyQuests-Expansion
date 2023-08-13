package fr.skytasul.quests.expansion.tracking;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.live.bemmamin.gps.api.GPSAPI;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.PreciseLocatable;
import fr.skytasul.quests.expansion.BeautyQuestsExpansion;
import fr.skytasul.quests.expansion.api.tracking.Tracker;

public class GpsTracker extends Tracker {

	private static final GPSAPI API = new GPSAPI(BeautyQuestsExpansion.getInstance());

	private final List<Player> tracked = new ArrayList<Player>();
	private PreciseLocatable locatable;

	@Override
	public Tracker clone() {
		return new GpsTracker();
	}

	@Override
	public void start(Locatable locatable) {
		this.locatable = (PreciseLocatable) locatable;
	}

	@Override
	public void stop() {
		locatable = null;
		tracked.forEach(API::stopGPS);
		tracked.clear();
	}

	@Override
	public void show(Player player) {
		Location location = locatable.getLocated().getLocation();
		if (location == null)
			return;
		if (API.gpsIsActive(player))
			return;
		API.startCompass(player, location);
		tracked.add(player);
	}

	@Override
	public void hide(Player player) {
		if (tracked.remove(player))
			API.stopGPS(player);
	}

	@Override
	protected void itemClick(QuestObjectClickEvent event) {}

}
