package fr.skytasul.quests.expansion.api.tracking;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.expansion.BeautyQuestsExpansion;

public abstract class Tracker extends QuestObject {
	
	protected Tracker() {
		super(BeautyQuestsExpansion.getInstance().getTrackersRegistry());
	}
	
	@Override
	public abstract Tracker clone();
	
	public abstract void start(Locatable locatable);
	
	public abstract void stop();
	
	public abstract void show(Player player);
	
	public abstract void hide(Player player);
	
}
