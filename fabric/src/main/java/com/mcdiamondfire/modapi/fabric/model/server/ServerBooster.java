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
	 * An active network booster.
	 *
	 * @param tipped        whether the connected player has tipped this booster
	 * @param multiplier    the token multiplier, such as {@code 2} for double tokens
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
