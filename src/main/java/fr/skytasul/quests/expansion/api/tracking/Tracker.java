package fr.skytasul.quests.expansion.api.tracking;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.expansion.BeautyQuestsExpansion;

public abstract class Tracker extends QuestObject {
	
	protected Tracker() {
		super(BeautyQuestsExpansion.getInstance().getTrackersRegistry(), null);
	}
	
	@Override
	public abstract Tracker clone();
	
	public abstract void start(Locatable locatable);
	
	public abstract void stop();
	
	public abstract void show(Player player);
	
	public abstract void hide(Player player);
	
	@Override
	protected final void clickInternal(QuestObjectClickEvent event) {
		itemClick(event);
	}

	protected abstract void itemClick(QuestObjectClickEvent event);

	@Override
	protected final String getDefaultDescription(Player p) {
		return null;
	}

	@Override
	protected ClickType getCustomDescriptionClick() {
		return null;
	}

	@Override
	protected void sendCustomDescriptionHelpMessage(Player p) {
		throw new UnsupportedOperationException();
	}

}
