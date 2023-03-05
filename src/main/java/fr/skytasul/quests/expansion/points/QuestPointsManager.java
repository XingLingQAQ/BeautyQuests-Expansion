package fr.skytasul.quests.expansion.points;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.Nullable;
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
import fr.skytasul.quests.commands.revxrsal.bukkit.annotation.CommandPermission;
import fr.skytasul.quests.commands.revxrsal.command.ExecutableCommand;
import fr.skytasul.quests.commands.revxrsal.orphan.OrphanCommand;
import fr.skytasul.quests.expansion.BeautyQuestsExpansion;
import fr.skytasul.quests.expansion.utils.LangExpansion;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.PlayersManagerDB;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.DependenciesManager.BQDependency;

public class QuestPointsManager implements OrphanCommand {
	
	protected SavableData<Integer> pointsData = new SavableData<>("points", Integer.class, 0);
	
	@Nullable
	private QuestPointsLeaderboard leaderboard;

	public QuestPointsManager() {
		BeautyQuests.getInstance().getPlayersManager().addAccountData(pointsData);
		
		if (BeautyQuests.getInstance().getPlayersManager() instanceof PlayersManagerDB) {
			leaderboard =
					new QuestPointsLeaderboard(this, ((PlayersManagerDB) BeautyQuests.getInstance().getPlayersManager()));
		} else {
			BeautyQuestsExpansion.logger.warning(
					"You are not using a database to save BeautyQuests datas. Quest points leaderboard is disabled.");
		}

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

		BeautyQuests.getInstance().dependencies
				.addDependency(new BQDependency("Rankup", () -> {
					Bukkit.getPluginManager().registerEvents(new QuestPointsRankup(this),
							BeautyQuests.getInstance());
					BeautyQuestsExpansion.logger
							.info("Registered Rankup quest points requirements.");
				}));
		BeautyQuests.getInstance().dependencies
				.addDependency(new BQDependency("PlaceholderAPI", () -> {
					new QuestPointsPlaceholders(this).register();
					BeautyQuestsExpansion.logger.info("Registered quest points placeholders.");
				}));
	}
	
	public int getPoints(PlayerAccount acc) {
		return acc.getData(pointsData);
	}
	
	public void addPoints(PlayerAccount acc, int points) {
		acc.setData(pointsData, getPoints(acc) + points);
	}
	
	@Nullable
	public QuestPointsLeaderboard getLeaderboard() {
		return leaderboard;
	}

	@Default
	@CommandPermission (value = "beautyquests.expansion.command.points", defaultAccess = PermissionDefault.TRUE)
	public void pointsSelf(BukkitCommandActor actor, ExecutableCommand command, @Optional String subcommand) {
		if (subcommand != null) throw new fr.skytasul.quests.commands.revxrsal.exception.InvalidSubcommandException(command.getPath(), subcommand);
		int points = getPoints(PlayersManager.getPlayerAccount(actor.requirePlayer()));
		LangExpansion.Points_Command_Balance.send(actor.getSender(), points);
	}
	
	@Subcommand ("get")
	@CommandPermission (value = "beautyquests.expansion.command.points.get", defaultAccess = PermissionDefault.OP)
	public void pointsGet(BukkitCommandActor actor, Player player) {
		int points = getPoints(PlayersManager.getPlayerAccount(player));
		LangExpansion.Points_Command_Balance_Player.send(actor.getSender(), points, player.getName());
	}
	
	@Subcommand ("add")
	@CommandPermission (value = "beautyquests.expansion.command.points.add", defaultAccess = PermissionDefault.OP)
	public void pointsAdd(BukkitCommandActor actor, Player player, int points) {
		addPoints(PlayersManager.getPlayerAccount(player), points);
		LangExpansion.Points_Command_Added.send(actor.getSender(), points, player.getName());
	}
	
}
