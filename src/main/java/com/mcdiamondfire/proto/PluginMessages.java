package com.mcdiamondfire.proto;

import com.google.protobuf.Message;

import java.util.*;

/**
 * Holds a mapping of plugin message classes to their string identifiers.
 *
 * @since 1.0.0
 */
public final class PluginMessages {
	
	private static final Map<Class<? extends Message>, String> messages = new HashMap<>();
	
	static {
		// Server.
		messages.put(ServerInfo.class, "server_info");
		messages.put(ServerBooster.class, "server_booster");
		
		// Plot.
		messages.put(PlotInfo.class, "plot_info");
		
		// Player.
		messages.put(PlayerCurrency.class, "player_currency");
		messages.put(PlayerPermissions.class, "player_permissions");
		messages.put(PlayerSwitchMode.class, "player_switch_mode");
	}
	
	private PluginMessages() {
		// Prevent instantiation.
	}
	
	/**
	 * Gets the identifier for a given plugin message class.
	 *
	 * @param clazz the plugin message class
	 * @return the identifier for the message class, or null if not registered
	 * @since 1.0.0
	 */
	public static String getMessageId(final Class<? extends Message> clazz) {
		return messages.get(clazz);
	}
	
	/**
	 * Gets an unmodifiable view of the registered plugin messages.
	 *
	 * @return a mapping of message classes to their identifiers
	 * @since 1.0.0
	 */
	public static Map<Class<? extends Message>, String> getMessages() {
		return Collections.unmodifiableMap(messages);
	}
	
}
