package fr.skytasul.quests.expansion.points;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.commands.revxrsal.annotation.Default;
import fr.skytasul.quests.api.commands.revxrsal.annotation.Optional;
import fr.skytasul.quests.api.commands.revxrsal.annotation.Subcommand;
import fr.skytasul.quests.api.commands.revxrsal.bukkit.BukkitCommandActor;
import fr.skytasul.quests.api.commands.revxrsal.bukkit.annotation.CommandPermission;
import fr.skytasul.quests.api.commands.revxrsal.command.ExecutableCommand;
import fr.skytasul.quests.api.commands.revxrsal.exception.InvalidSubcommandException;
import fr.skytasul.quests.api.commands.revxrsal.orphan.OrphanCommand;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.api.utils.IntegrationManager.BQDependency;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.expansion.BeautyQuestsExpansion;
import fr.skytasul.quests.expansion.ExpansionConfiguration.QuestPointsConfiguration;
import fr.skytasul.quests.expansion.utils.LangExpansion;
import fr.skytasul.quests.players.PlayersManagerDB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuestPointsManager implements OrphanCommand {

	protected final SavableData<Integer> pointsData = new SavableData<>("points", Integer.class, 0);

	@Nullable
	private QuestPointsLeaderboard leaderboard;

	@NotNull
	private QuestPointsConfiguration config;

	public QuestPointsManager(@NotNull QuestPointsConfiguration config) {
		this.config = config;

		QuestsPlugin.getPlugin().getPlayersManager().addAccountData(pointsData);

		if (QuestsPlugin.getPlugin().getPlayersManager() instanceof PlayersManagerDB) {
			leaderboard =
					new QuestPointsLeaderboard(this, ((PlayersManagerDB) QuestsPlugin.getPlugin().getPlayersManager()));
		} else {
			BeautyQuestsExpansion.logger.warning(
					"You are not using a database to save BeautyQuests datas. Quest points leaderboard is disabled.");
		}

		QuestsAPI.getAPI().getRewards().register(new RewardCreator(
				"points",
				QuestPointsReward.class,
				ItemUtils.item(XMaterial.EMERALD, "§a" + LangExpansion.Points_Name.toString(),
						QuestOption.formatDescription(LangExpansion.Points_Reward_Description.toString()),
						"",
						QuestOption.formatDescription(LangExpansion.Points_Description.toString()),
						"",
						LangExpansion.Expansion_Label.toString()),
				QuestPointsReward::new));
		QuestsAPI.getAPI().getRequirements().register(new RequirementCreator(
				"points",
				QuestPointsRequirement.class,
				ItemUtils.item(XMaterial.EMERALD, "§a" + LangExpansion.Points_Name.toString(),
						QuestOption.formatDescription(LangExpansion.Points_Requirement_Description.toString()),
						"",
						QuestOption.formatDescription(LangExpansion.Points_Description.toString()),
						"",
						LangExpansion.Expansion_Label.toString()),
				QuestPointsRequirement::new));

		QuestsPlugin.getPlugin().getCommand().registerCommands("points", this);

		QuestsPlugin.getPlugin().getIntegrationManager()
				.addDependency(new BQDependency("Rankup", () -> {
					Bukkit.getPluginManager().registerEvents(new QuestPointsRankup(this), QuestsPlugin.getPlugin());
					BeautyQuestsExpansion.logger
							.info("Registered Rankup quest points requirements.");
				}));
		QuestsPlugin.getPlugin().getIntegrationManager()
				.addDependency(new BQDependency("PlaceholderAPI", () -> {
					new QuestPointsPlaceholders(this).register();
					BeautyQuestsExpansion.logger.info("Registered quest points placeholders.");
				}));
	}

	public int getPoints(PlayerAccount acc) {
		return acc.getData(pointsData);
	}

	public void addPoints(PlayerAccount acc, int points) throws IllegalPointsBalanceException {
		int newBalance = getPoints(acc) + points;
		if (newBalance < 0) {
			switch (config.getNegativeBehavior()) {
				case ALLOW:
					// nothing to do here
					break;
				case FAIL:
					throw new IllegalPointsBalanceException(acc, newBalance);
				case FLOOR:
					newBalance = 0;
					break;
			}
		}

		acc.setData(pointsData, newBalance);
	}

	public void unload() {
		if (leaderboard != null)
			leaderboard.unload();
	}

	@Nullable
	public QuestPointsLeaderboard getLeaderboard() {
		return leaderboard;
	}

	@Default
	@CommandPermission (value = "beautyquests.expansion.command.points", defaultAccess = PermissionDefault.TRUE)
	public void pointsSelf(BukkitCommandActor actor, ExecutableCommand command, @Optional String subcommand) {
		if (subcommand != null) throw new InvalidSubcommandException(command.getPath(), subcommand);
		int points = getPoints(PlayersManager.getPlayerAccount(actor.requirePlayer()));
		LangExpansion.Points_Command_Balance.quickSend(actor.getSender(), "quest_points_balance", points);
	}

	@Subcommand ("get")
	@CommandPermission (value = "beautyquests.expansion.command.points.get", defaultAccess = PermissionDefault.OP)
	public void pointsGet(BukkitCommandActor actor, Player player) {
		int points = getPoints(PlayersManager.getPlayerAccount(player));
		LangExpansion.Points_Command_Balance_Player.send(actor.getSender(),
				PlaceholderRegistry.of("quest_points_balance", points, "target_name", player.getName()));
	}

	@Subcommand ("add")
	@CommandPermission (value = "beautyquests.expansion.command.points.add", defaultAccess = PermissionDefault.OP)
	public void pointsAdd(BukkitCommandActor actor, Player player, int points) throws IllegalPointsBalanceException {
		addPoints(PlayersManager.getPlayerAccount(player), points);
		LangExpansion.Points_Command_Added.send(actor.getSender(),
				PlaceholderRegistry.of("quest_points_balance", points, "target_name", player.getName()));
	}

}
