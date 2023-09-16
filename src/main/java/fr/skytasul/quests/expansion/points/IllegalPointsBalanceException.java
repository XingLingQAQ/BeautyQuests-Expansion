package fr.skytasul.quests.expansion.points;

import fr.skytasul.quests.api.players.PlayerAccount;

public class IllegalPointsBalanceException extends Exception {

	private static final long serialVersionUID = 8142562529319509619L;

	public IllegalPointsBalanceException(PlayerAccount account, int illegalBalance) {
		super("Illegal quest points balance for " + account.getNameAndID() + ": " + illegalBalance);
	}

}
