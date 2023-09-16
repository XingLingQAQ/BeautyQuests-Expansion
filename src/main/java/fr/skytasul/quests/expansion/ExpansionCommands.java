package fr.skytasul.quests.expansion;

import java.util.StringJoiner;
import fr.skytasul.quests.api.commands.revxrsal.annotation.Subcommand;
import fr.skytasul.quests.api.commands.revxrsal.bukkit.BukkitCommandActor;
import fr.skytasul.quests.api.commands.revxrsal.bukkit.annotation.CommandPermission;
import fr.skytasul.quests.api.commands.revxrsal.orphan.OrphanCommand;
import fr.skytasul.quests.api.utils.messaging.MessageType.DefaultMessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.expansion.utils.LangExpansion;

public class ExpansionCommands implements OrphanCommand {

	@Subcommand ("expansion")
	@CommandPermission ("beautyquests.expansion.command.expansion")
	public void expansion(BukkitCommandActor actor) {
		StringJoiner joiner = new StringJoiner("\n");
		joiner.add(LangExpansion.Features_Header.quickFormat("features_amount",
				BeautyQuestsExpansion.getInstance().getFeatures().size()));
		for (ExpansionFeature feature : BeautyQuestsExpansion.getInstance().getFeatures()) {
			joiner.add("- " + feature.toString());
		}
		MessageUtils.sendMessage(actor.getSender(), joiner.toString(), DefaultMessageType.PREFIXED);
	}

}
