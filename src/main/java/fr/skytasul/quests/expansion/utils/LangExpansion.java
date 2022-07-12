package fr.skytasul.quests.expansion.utils;

import fr.skytasul.quests.api.Locale;
import fr.skytasul.quests.utils.Lang;

public enum LangExpansion implements Locale {
	
	Expansion_Label("expansion.label"),
	
	Features_Header("features.header"), // 0: amount enabled
	Features_Unloaded("features.unloaded"),
	
	TimeLimit_Name("timelimit.name"),
	TimeLimit_Description("timelimit.description"),
	TimeLimit_Left("timelimit.left"), // 0: left duration
	TimeLimit_EDITOR("timelimit.editor", Lang.EditorPrefix),
	
	Tracking_Trackers("tracking.trackers"), // 0: tracker amount
	Tracking_Name("tracking.name"),
	Tracking_Description("tracking.description"),
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
	private Locale prefix;
	
	private LangExpansion(String path) {
		this(path, null);
	}
	
	private LangExpansion(String path, Locale prefix) {
		this.path = path;
		this.prefix = prefix;
	}
	
	@Override
	public String getPath() {
		return path;
	}
	
	@Override
	public String getValue() {
		return prefix == null ? value : (prefix.toString() + value);
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
