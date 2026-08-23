package com.mcdiamondfire.modapi.fabric.client;

import com.mcdiamondfire.modapi.fabric.code.CodeAPI;
import com.mcdiamondfire.modapi.fabric.event.ModAPIEvent;
import com.mcdiamondfire.modapi.fabric.event.ModAPISignal;
import com.mcdiamondfire.modapi.fabric.internal.InternalRuntime;
import com.mcdiamondfire.modapi.fabric.model.Location;
import com.mcdiamondfire.modapi.fabric.model.player.ChestReference;
import com.mcdiamondfire.modapi.fabric.model.player.PlayerInfo;
import com.mcdiamondfire.modapi.fabric.model.player.PlayerMode;
import com.mcdiamondfire.modapi.fabric.model.plot.CodeLineStarter;
import com.mcdiamondfire.modapi.fabric.model.plot.LineStarterUpdate;
import com.mcdiamondfire.modapi.fabric.model.plot.PlotInfo;
import com.mcdiamondfire.modapi.fabric.model.server.ServerBooster;
import com.mcdiamondfire.modapi.fabric.model.server.ServerInfo;

import java.util.List;
import java.util.Optional;

/**
 * The central API entrypoint for DiamondFire.
 */
public final class ModAPI {
	
	/**
	 * Code space queries.
	 */
	public static final CodeAPI CODE = new CodeAPI();
	
	/**
	 * Fired after the server accepts the ModAPI handshake.
	 */
	public static final ModAPIEvent<ServerInfo> ON_CONNECT = new ModAPIEvent<>();
	
	/**
	 * Fired when the ModAPI handshake fails or is rejected.
	 * <p>
	 * Treat {@link HandshakeException handshake exceptions} as user errors,
	 * other exceptions are unexpected and considered bugs.
	 * </p>
	 *
	 * @see HandshakeException
	 */
	public static final ModAPIEvent<Throwable> ON_CONNECTION_FAILED = new ModAPIEvent<>();
	
	/**
	 * Fired when an established ModAPI connection closes.
	 */
	public static final ModAPISignal ON_DISCONNECT = new ModAPISignal();
	
	/**
	 * Fired when the player joins the server.
	 */
	public static final ModAPIEvent<PlayerInfo> ON_PLAYER_JOIN = new ModAPIEvent<>();
	
	/**
	 * Fired when a booster is activated, when a player tips, and when joining the server.
	 */
	public static final ModAPIEvent<ServerBooster> ON_BOOSTER_UPDATE = new ModAPIEvent<>();
	
	/**
	 * Fired when the player switches modes.
	 */
	public static final ModAPIEvent<PlayerMode> ON_MODE_SWITCH = new ModAPIEvent<>();
	
	/**
	 * Fired when the player opens a code action chest.
	 */
	public static final ModAPIEvent<ChestReference> ON_CODE_CHEST_OPEN = new ModAPIEvent<>();
	
	/**
	 * Fired when the player joins a plot.
	 */
	public static final ModAPIEvent<PlotInfo> ON_JOIN_PLOT = new ModAPIEvent<>();
	
	/**
	 * Fired with the line starters present when a developer joins a plot.
	 */
	public static final ModAPIEvent<List<CodeLineStarter>> ON_LINE_STARTER_SEND = new ModAPIEvent<>();
	
	/**
	 * Fired when a line starter is added, changed, or removed.
	 */
	public static final ModAPIEvent<LineStarterUpdate> ON_LINE_STARTER_UPDATE = new ModAPIEvent<>();
	
	/**
	 * Fired once per second with the active plot's CPU usage percentage while the player is profiling it.
	 */
	public static final ModAPIEvent<Integer> ON_PLOT_PROFILE = new ModAPIEvent<>();
	
	private ModAPI() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Returns whether the player is connected to the ModAPI.
	 *
	 * @return whether the API is ready for requests
	 */
	public static boolean isConnected() {
		return InternalRuntime.isConnected();
	}
	
	/**
	 * Returns information about the connected server.
	 *
	 * @return the server information or empty before the handshake succeeds
	 */
	public static Optional<ServerInfo> getServerInfo() {
		return InternalRuntime.getServerInfo();
	}
	
	/**
	 * Teleports the player. The player must be in build or dev mode, and the
	 * server clamps the destination to the plot boundaries.
	 *
	 * @param location the requested location
	 */
	public static void teleport(Location location) {
		InternalRuntime.teleport(location);
	}
	
}
