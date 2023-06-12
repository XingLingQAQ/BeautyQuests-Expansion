package fr.skytasul.quests.expansion;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.expansion.points.NegativePointsBehavior;

public class ExpansionConfiguration {

	private String lang;
	private QuestPointsConfiguration points;

	public ExpansionConfiguration(ConfigurationSection config) {
		lang = config.getString("lang");

		points = new QuestPointsConfiguration(config.getConfigurationSection("points"));
	}

	public String getLang() {
		return lang;
	}

	public @NotNull QuestPointsConfiguration getPointsConfig() {
		return points;
	}

	public class QuestPointsConfiguration {

		private NegativePointsBehavior negativeBehavior;

		public QuestPointsConfiguration(ConfigurationSection config) {
			try {
				negativeBehavior = NegativePointsBehavior.valueOf(config.getString("negative behavior", "").toUpperCase());
			} catch (IllegalArgumentException ex) {
				negativeBehavior = NegativePointsBehavior.ALLOW;
				BeautyQuestsExpansion.logger.warning("Incorrect value for negative behavior: "
						+ config.getString("negative behavior") + ". Falling back to ALLOW.");
			}
		}

		public @NotNull NegativePointsBehavior getNegativeBehavior() {
			return negativeBehavior;
		}

	}

}
