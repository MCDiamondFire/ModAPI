package com.mcdiamondfire.modapi.fabric.model.player;

import java.util.List;

/**
 * Information about the player.
 *
 * @param currencies the player's current currencies
 * @param ranks      the player's ranks
 */
public record PlayerInfo(Currencies currencies, Ranks ranks) {
	
	/**
	 * The player's currency balances.
	 *
	 * @param tokens        the player's token balance
	 * @param tickets       the player's ticket balance
	 * @param ticketBundles unclaimed ticket bundles
	 * @param sparks        the player's spark balance
	 */
	public record Currencies(int tokens, int tickets, List<TicketBundle> ticketBundles, int sparks) {
		
		/**
		 * Creates immutable currency information.
		 */
		public Currencies {
			ticketBundles = List.copyOf(ticketBundles);
		}
		
	}
	
	/**
	 * An unclaimed ticket prize.
	 *
	 * @param eventName    the event that awarded the bundle
	 * @param prizeName    the prize represented by the bundle
	 * @param ticketAmount the number of tickets in the bundle
	 */
	public record TicketBundle(String eventName, String prizeName, int ticketAmount) {
	
	}
	
}
