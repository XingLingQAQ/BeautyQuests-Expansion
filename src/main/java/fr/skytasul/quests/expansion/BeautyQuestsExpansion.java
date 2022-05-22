package fr.skytasul.quests.expansion;

import org.bukkit.plugin.java.JavaPlugin;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.BeautyQuests.LoadingException;
import fr.skytasul.quests.api.Locale;
import fr.skytasul.quests.expansion.api.tracking.TrackerRegistry;
import fr.skytasul.quests.expansion.utils.LangExpansion;
import fr.skytasul.quests.utils.logger.LoggerExpanded;

public class BeautyQuestsExpansion extends JavaPlugin {
	
	public static LoggerExpanded logger;
	private static BeautyQuestsExpansion instance;
	
	public static BeautyQuestsExpansion getInstance() {
		return instance;
	}
	
	private TrackerRegistry trackersRegistry;
	
	@Override
	public void onLoad() {
		instance = this;
		logger = new LoggerExpanded(getLogger());
	}
	
	@Override
	public void onEnable() {
		try {
			logMessage("Hooked expansion version " + getDescription().getVersion());
			
			loadLang();
			
			trackersRegistry = new TrackerRegistry();
		}catch (LoadingException ex) {
			if (ex.getCause() != null) logger.severe("A fatal error occurred while loading plugin.", ex.getCause());
			logger.severe(ex.getLoggerMessage());
			logger.severe("This is a fatal error. Now disabling.");
			setEnabled(false);
		}
	}
	
	private void loadLang() throws LoadingException {
		try {
			Locale.loadLang(this, LangExpansion.values(), "en_US", "en_US");
		}catch (Exception ex) {
			throw new LoadingException("Couldn't load language file.", ex);
		}
	}
	
	public TrackerRegistry getTrackersRegistry() {
		return trackersRegistry;
	}
	
	public void logMessage(String message) {
		BeautyQuests.getInstance().getLoggerHandler().write("[EXPANSION] " + message);
	}
	
}
