package fr.skytasul.quests.expansion;

import java.util.StringJoiner;

import fr.skytasul.quests.commands.revxrsal.annotation.Subcommand;
import fr.skytasul.quests.commands.revxrsal.bukkit.BukkitCommandActor;
import fr.skytasul.quests.commands.revxrsal.bukkit.annotation.CommandPermission;
import fr.skytasul.quests.commands.revxrsal.orphan.OrphanCommand;
import fr.skytasul.quests.expansion.utils.LangExpansion;
import fr.skytasul.quests.utils.Utils;

public class ExpansionCommands implements OrphanCommand {
	
	@Subcommand ("expansion")
	@CommandPermission ("beautyquests.expansion.command.expansion")
	public void expansion(BukkitCommandActor actor) {
		StringJoiner joiner = new StringJoiner("\n");
		joiner.add(LangExpansion.Features_Header.format(BeautyQuestsExpansion.getInstance().getFeatures().size()));
		for (ExpansionFeature feature : BeautyQuestsExpansion.getInstance().getFeatures()) {
			joiner.add("- " + feature.toString());
		}
		Utils.sendMessage(actor.getSender(), joiner.toString());
	}
	
}
