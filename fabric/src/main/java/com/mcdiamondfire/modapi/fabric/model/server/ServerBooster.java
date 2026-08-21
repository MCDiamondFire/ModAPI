package com.mcdiamondfire.modapi.fabric.model.server;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * The current network booster state.
 *
 * @param activeBooster the active booster, or empty when no booster is active
 */
public record ServerBooster(Optional<ActiveBooster> activeBooster) {
	
	/**
	 * Returns whether a booster is active.
	 *
	 * @return whether {@link #activeBooster()} is present
	 */
	public boolean isActive() {
		return activeBooster.isPresent();
	}
	
	/**
	 * An active server booster.
	 *
	 * @param tipped        whether the player has tipped the booster
	 * @param multiplier    the token multiplier
	 * @param timeRemaining how long the booster remains active
	 * @param ownerName     the name of the player who activated the booster
	 * @param ownerId       the UUID of the player who activated the booster
	 */
	public record ActiveBooster(
			boolean tipped,
			int multiplier,
			Duration timeRemaining,
			String ownerName,
			UUID ownerId
	) {
	
	}
	
}
