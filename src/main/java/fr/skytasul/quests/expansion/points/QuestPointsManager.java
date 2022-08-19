package fr.skytasul.quests.expansion.points;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.commands.Cmd;
import fr.skytasul.quests.commands.CommandContext;
import fr.skytasul.quests.expansion.utils.LangExpansion;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class QuestPointsManager {
	
	private SavableData<Integer> pointsData = new SavableData<>("points", Integer.class, 0);
	
	public QuestPointsManager() {
		PlayersManager.manager.addAccountData(pointsData);
		
		QuestsAPI.getRewards().register(new RewardCreator(
				"points",
				QuestPointsReward.class,
				ItemUtils.item(XMaterial.EMERALD, "§a" + LangExpansion.Points_Name.toString(),
						QuestOption.formatDescription(LangExpansion.Points_Reward_Description.toString()),
						"",
						QuestOption.formatDescription(LangExpansion.Points_Description.toString()),
						"",
						LangExpansion.Expansion_Label.toString()),
				QuestPointsReward::new));
		QuestsAPI.getRequirements().register(new RequirementCreator(
				"points",
				QuestPointsRequirement.class,
				ItemUtils.item(XMaterial.EMERALD, "§a" + LangExpansion.Points_Name.toString(),
						QuestOption.formatDescription(LangExpansion.Points_Requirement_Description.toString()),
						"",
						QuestOption.formatDescription(LangExpansion.Points_Description.toString()),
						"",
						LangExpansion.Expansion_Label.toString()),
				QuestPointsRequirement::new));
		
		BeautyQuests.getInstance().getCommand().registerCommandsClass(this);
	}
	
	public int getPoints(PlayerAccount acc) {
		return acc.getData(pointsData);
	}
	
	public void addPoints(PlayerAccount acc, int points) {
		acc.setData(pointsData, getPoints(acc) + points);
	}
	
	@Cmd
	public void points(CommandContext cmd) {
		if (cmd.args.length == 0) {
			if (cmd.isPlayer()) {
				int points = getPoints(PlayersManager.getPlayerAccount(cmd.player));
				LangExpansion.Points_Command_Balance.send(cmd.player, points);
			}else {
				Lang.MUST_PLAYER.send(cmd.sender);
			}
		}
	}
	
}
