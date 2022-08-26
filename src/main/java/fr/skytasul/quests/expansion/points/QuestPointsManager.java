package fr.skytasul.quests.expansion.points;

import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.commands.revxrsal.annotation.Default;
import fr.skytasul.quests.commands.revxrsal.annotation.Optional;
import fr.skytasul.quests.commands.revxrsal.annotation.Subcommand;
import fr.skytasul.quests.commands.revxrsal.bukkit.BukkitCommandActor;
import fr.skytasul.quests.commands.revxrsal.command.ExecutableCommand;
import fr.skytasul.quests.commands.revxrsal.orphan.OrphanCommand;
import fr.skytasul.quests.expansion.utils.LangExpansion;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.XMaterial;

public class QuestPointsManager implements OrphanCommand {
	
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
		
		BeautyQuests.getInstance().getCommand().registerCommands("points", this);
	}
	
	public int getPoints(PlayerAccount acc) {
		return acc.getData(pointsData);
	}
	
	public void addPoints(PlayerAccount acc, int points) {
		acc.setData(pointsData, getPoints(acc) + points);
	}
	
	@Default
	public void pointsSelf(BukkitCommandActor actor, ExecutableCommand command, @Optional String subcommand) {
		if (subcommand != null) throw new fr.skytasul.quests.commands.revxrsal.exception.InvalidSubcommandException(command.getPath(), subcommand);
		int points = getPoints(PlayersManager.getPlayerAccount(actor.requirePlayer()));
		LangExpansion.Points_Command_Balance.send(actor.getSender(), points);
	}
	
	@Subcommand ("get")
	public void pointsGet(BukkitCommandActor actor, Player player) {
		int points = getPoints(PlayersManager.getPlayerAccount(player));
		LangExpansion.Points_Command_Balance_Player.send(actor.getSender(), points, player.getName());
	}
	
	@Subcommand ("add")
	public void pointsAdd(BukkitCommandActor actor, Player player, int points) {
		addPoints(PlayersManager.getPlayerAccount(player), points);
		LangExpansion.Points_Command_Added.send(actor.getSender(), points, player.getName());
	}
	
}
