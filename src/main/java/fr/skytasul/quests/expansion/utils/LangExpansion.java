package fr.skytasul.quests.expansion.utils;

import fr.skytasul.quests.api.Locale;

public enum LangExpansion implements Locale {
	
	Expansion_Label("expansion.label"),
	Tracking_Trackers("tracking.trackers"), // 0: tracker amount
	Tracking_Gui_Name("tracking.gui.name"),
	Tracking_Particles_Name("tracking.particles.name"),
	Tracking_Particles_Description("tracking.particles.description"),
	Tracking_Outline_Name("tracking.outline.name"),
	Tracking_Outline_Description("tracking.outline.description"),
	Tracking_Beacon_Name("tracking.beacon.name"),
	Tracking_Beacon_Description("tracking.beacon.description"),
	Tracking_Glowing_Name("tracking.glowing.name"),
	Tracking_Glowing_Description("tracking.glowing.description"),
	
	;
	
	private final String path;
	
	private String value = "§cnot loaded";
	
	private LangExpansion(String path) {
		this.path = path;
	}
	
	@Override
	public String getPath() {
		return path;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	@Override
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return getValue();
	}

}
