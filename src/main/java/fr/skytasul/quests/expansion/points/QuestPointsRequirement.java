package fr.skytasul.quests.expansion.points;

import org.bukkit.entity.Player;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.expansion.BeautyQuestsExpansion;
import fr.skytasul.quests.expansion.utils.LangExpansion;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.ComparisonMethod;

public class QuestPointsRequirement extends TargetNumberRequirement {
	
	public QuestPointsRequirement() {
		this(null, null, 0, ComparisonMethod.GREATER_OR_EQUAL);
	}
	
	public QuestPointsRequirement(String customDescription, String customReason, double target,
			ComparisonMethod comparison) {
		super(customDescription, customReason, target, comparison);
	}
	
	@Override
	public double getPlayerTarget(Player p) {
		return BeautyQuestsExpansion.getInstance().getPointsManager().getPoints(PlayersManager.getPlayerAccount(p));
	}

	@Override
	public Class<? extends Number> numberClass() {
		return Integer.class;
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return LangExpansion.Points_Value.format(getShortFormattedValue());
	}
	
	@Override
	protected String getDefaultReason(Player player) {
		return LangExpansion.Points_Requirement_Message.format(getFormattedValue());
	}

	@Override
	public void sendHelpString(Player p) {
		LangExpansion.Points_Requirement_Editor_Target.send(p);
	}
	
	@Override
	public AbstractRequirement clone() {
		return new QuestPointsRequirement(getCustomDescription(), getCustomReason(), target, comparison);
	}
	
}
