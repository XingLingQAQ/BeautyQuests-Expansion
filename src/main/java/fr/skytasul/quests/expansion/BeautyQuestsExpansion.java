package fr.skytasul.quests.expansion;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.BeautyQuests.LoadingException;
import fr.skytasul.quests.api.Locale;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.expansion.api.tracking.TrackerRegistry;
import fr.skytasul.quests.expansion.options.TimeLimitOption;
import fr.skytasul.quests.expansion.utils.LangExpansion;
import fr.skytasul.quests.utils.logger.LoggerExpanded;

public class BeautyQuestsExpansion extends JavaPlugin {
	
	public static LoggerExpanded logger;
	private static BeautyQuestsExpansion instance;
	
	public static BeautyQuestsExpansion getInstance() {
		return instance;
	}
	
	private List<ExpansionFeature> features = new ArrayList<>();
	
	private TrackerRegistry trackersRegistry;
	
	@Override
	public void onLoad() {
		instance = this;
		logger = new LoggerExpanded(getLogger());
	}
	
	@Override
	public void onEnable() {
		try {
			logger.info("------- BeautyQuests Expansion -------");
			logger.info("Thank you for purchasing the expansion!");
			
			logMessage("Hooked expansion version " + getDescription().getVersion());
			
			loadLang();
			
			addDefaultFeatures();
			loadFeatures();
			
			registerCommands();
		}catch (LoadingException ex) {
			if (ex.getCause() != null) logger.severe("A fatal error occurred while loading plugin.", ex.getCause());
			logger.severe(ex.getLoggerMessage());
			logger.severe("This is a fatal error. Now disabling.");
			setEnabled(false);
		}
	}
	
	private void registerCommands() {
		BeautyQuests.getInstance().getCommand().registerCommandsClass(new ExpansionCommands());
	}
	
	@Override
	public void onDisable() {
		unloadFeatures();
	}
	
	private void loadLang() throws LoadingException {
		try {
			Locale.loadLang(this, LangExpansion.values(), "en_US", "en_US");
		}catch (Exception ex) {
			throw new LoadingException("Couldn't load language file.", ex);
		}
	}
	
	private void addDefaultFeatures() {
		features.add(new ExpansionFeature(
				LangExpansion.Tracking_Name.toString(),
				LangExpansion.Tracking_Description.toString(),
				() -> trackersRegistry = new TrackerRegistry(),
				null));
		features.add(new ExpansionFeature(
				LangExpansion.TimeLimit_Name.toString(),
				LangExpansion.TimeLimit_Description.toString(),
				() -> QuestsAPI.registerQuestOption(new QuestOptionCreator<>("timeLimit", 40, TimeLimitOption.class, TimeLimitOption::new, 0)),
				null));
	}
	
	private void loadFeatures() {
		int loaded = 0;
		
		for (ExpansionFeature feature : features) {
			if (feature.load()) loaded++;
		}
		
		logger.info(loaded + " expanded features have been loaded.");
	}
	
	private void unloadFeatures() {
		features.forEach(ExpansionFeature::unload);
	}
	
	public List<ExpansionFeature> getFeatures() {
		return features;
	}
	
	public TrackerRegistry getTrackersRegistry() {
		return trackersRegistry;
	}
	
	public void logMessage(String message) {
		BeautyQuests.getInstance().getLoggerHandler().write("[EXPANSION] " + message);
	}
	
}
