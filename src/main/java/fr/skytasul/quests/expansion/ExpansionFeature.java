package fr.skytasul.quests.expansion;

import fr.skytasul.quests.expansion.utils.LangExpansion;

public class ExpansionFeature {
	
	private final String name;
	private final String description;
	private final Runnable load;
	private final Runnable unload;
	
	private boolean loaded;
	
	public ExpansionFeature(String name, String description, Runnable load, Runnable unload) {
		this.name = name;
		this.description = description;
		this.load = load;
		this.unload = unload;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean load() {
		try {
			if (load != null) load.run();
			loaded = true;
		}catch (Throwable ex) {
			BeautyQuestsExpansion.logger.severe("An exception occurred while loading feature " + name, ex);
			loaded = false;
		}
		return loaded;
	}
	
	public void unload() {
		if (loaded && unload != null) unload.run();
	}
	
	@Override
	public String toString() {
		String string = (loaded ? "§a" : "§c") + getName() + ":§f " + getDescription();
		if (!loaded) string += " §c(" + LangExpansion.Features_Unloaded.toString() + ")";
		return string;
	}
	
}
