package fr.skytasul.quests.expansion;

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
	
}
