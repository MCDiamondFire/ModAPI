package com.mcdiamondfire.modapi.fabric.model.plot;

import com.mcdiamondfire.modapi.fabric.model.Location;
import com.mcdiamondfire.modapi.fabric.model.player.PlayerMode;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A plot.
 *
 * @param id          the plot identifier
 * @param name        the displayed plot name
 * @param ownerName   the plot owner's current name
 * @param ownerId     the plot owner's UUID
 * @param size        the plot size tier
 * @param buildRegion the build region
 * @param codeRegion  the code region
 * @param spawn       the plot's default spawn location
 * @param owner       whether the player owns the plot
 * @param developer   whether the player may edit the plot's code
 * @param builder     whether the player may build on the plot
 * @param tags        the plot's categories
 * @param handle      the plot handle, when set
 * @param players     players currently on the plot
 */
public record PlotInfo(
		int id,
		Component name,
		String ownerName,
		UUID ownerId,
		PlotSize size,
		Region buildRegion,
		Region codeRegion,
		Location spawn,
		boolean owner,
		boolean developer,
		boolean builder,
		List<PlotTag> tags,
		Optional<String> handle,
		List<PlotPlayer> players
) {
	
	/**
	 * Creates immutable plot information.
	 */
	public PlotInfo {
		tags = List.copyOf(tags);
		players = List.copyOf(players);
	}
	
	/**
	 * DiamondFire plot size tiers.
	 */
	public enum PlotSize {
		/// A basic plot.
		BASIC,
		/// A large plot.
		LARGE,
		/// A massive plot.
		MASSIVE,
		/// A mega plot.
		MEGA,
		/// A world plot.
		WORLD,
		/// A size introduced by a newer protocol version.
		UNKNOWN
	}
	
	/**
	 * A player currently present on a plot.
	 *
	 * @param name      the player's current name
	 * @param id        the player's UUID
	 * @param owner     whether the player owns the plot
	 * @param developer whether the player may edit the plot's code
	 * @param builder   whether the player may build on the plot
	 * @param mode      the player's current mode
	 */
	public record PlotPlayer(
			String name,
			UUID id,
			boolean owner,
			boolean developer,
			boolean builder,
			PlayerMode mode
	) {
	
	}
	
}
