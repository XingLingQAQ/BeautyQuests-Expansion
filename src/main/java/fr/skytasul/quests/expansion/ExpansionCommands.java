package fr.skytasul.quests.expansion;

import java.util.StringJoiner;

import fr.skytasul.quests.commands.Cmd;
import fr.skytasul.quests.commands.CommandContext;
import fr.skytasul.quests.expansion.utils.LangExpansion;
import fr.skytasul.quests.utils.Utils;

public class ExpansionCommands {
	
	@Cmd
	public void expansion(CommandContext cmd) {
		StringJoiner joiner = new StringJoiner("\n");
		joiner.add(LangExpansion.Features_Header.format(BeautyQuestsExpansion.getInstance().getFeatures().size()));
		for (ExpansionFeature feature : BeautyQuestsExpansion.getInstance().getFeatures()) {
			joiner.add("- " + feature.toString());
		}
		Utils.sendMessage(cmd.sender, joiner.toString());
	}
	
}
