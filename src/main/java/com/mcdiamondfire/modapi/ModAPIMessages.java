package com.mcdiamondfire.modapi;

import com.google.protobuf.Message;
import com.mcdiamondfire.modapi.messages.clientbound.player.S2CChestReference;
import com.mcdiamondfire.modapi.messages.clientbound.player.S2CPlayerSwitchMode;
import com.mcdiamondfire.modapi.messages.clientbound.plot.*;
import com.mcdiamondfire.modapi.messages.clientbound.server.S2CHandshakeResponse;
import com.mcdiamondfire.modapi.messages.clientbound.server.S2CHello;
import com.mcdiamondfire.modapi.messages.clientbound.server.S2CServerBooster;
import com.mcdiamondfire.modapi.messages.serverbound.player.C2SPlayerTeleport;
import com.mcdiamondfire.modapi.messages.serverbound.plot.C2SCodeOperation;
import com.mcdiamondfire.modapi.messages.serverbound.plot.C2SMultiCodeOperations;
import com.mcdiamondfire.modapi.messages.serverbound.server.C2SHandshakeRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Holds a mapping of ModAPI message classes to their string identifiers.
 */
public final class ModAPIMessages {
	
	private static final Map<Class<? extends Message>, String> CLASS_ID_MAP = new HashMap<>();
	private static final Map<String, Class<? extends Message>> ID_CLASS_MAP = new HashMap<>();
	
	static {
		
		// Client-bound.
		
		// Server.
		registerMessage(S2CHello.class, "s2c_hello");
		registerMessage(S2CHandshakeResponse.class, "s2c_handshake_response");
		registerMessage(S2CServerBooster.class, "s2c_server_booster");
		
		// Plot.
		registerMessage(S2CPlotInfo.class, "s2c_plot_info");
		registerMessage(S2CCodeOperationResult.class, "s2c_code_operation_result");
		registerMessage(S2CMultiCodeOperationsResult.class, "s2c_multi_code_operations_result");
		registerMessage(S2CPlotLineStarters.class, "s2c_plot_line_starters");
		registerMessage(S2CPlotLineStarterUpdate.class, "s2c_plot_line_starter_update");
		registerMessage(S2CPlotProfiling.class, "s2c_plot_profiling");
		
		// Player.
		registerMessage(S2CPlayerSwitchMode.class, "s2c_player_switch_mode");
		registerMessage(S2CChestReference.class, "s2c_chest_reference");
		
		// Server-bound.
		
		// Server.
		registerMessage(C2SHandshakeRequest.class, "mc2s_handshake_request");
		
		// Plot.
		registerMessage(C2SCodeOperation.class, "c2s_code_operation");
		registerMessage(C2SMultiCodeOperations.class, "c2s_multi_code_operations");
		
		// Player.
		registerMessage(C2SPlayerTeleport.class, "c2s_player_teleport");
		
	}
	
	private ModAPIMessages() {
		// Prevent instantiation.
	}
	
	private static void registerMessage(Class<? extends Message> clazz, String id) {
		if (CLASS_ID_MAP.containsKey(clazz)) {
			throw new IllegalStateException("Message class already registered: " + clazz.getName());
		}
		if (ID_CLASS_MAP.containsKey(id)) {
			throw new IllegalStateException("Message ID already registered: " + id);
		}
		CLASS_ID_MAP.put(clazz, id);
		ID_CLASS_MAP.put(id, clazz);
	}
	
	/**
	 * Gets the identifier for a given ModAPI message class.
	 *
	 * @param clazz the ModAPI message class
	 * @return an Optional containing the identifier or empty if not registered
	 */
	public static Optional<String> getMessageId(Class<? extends Message> clazz) {
		return Optional.ofNullable(CLASS_ID_MAP.get(clazz));
	}
	
	/**
	 * Gets the ModAPI message class for a given identifier.
	 *
	 * @param id the identifier of the ModAPI message
	 * @return an Optional containing the ModAPI message class or empty if not registered
	 */
	public static Optional<Class<? extends Message>> getMessageClass(String id) {
		return Optional.ofNullable(ID_CLASS_MAP.get(id));
	}
	
	/**
	 * Gets an unmodifiable view of the registered ModAPI messages.
	 *
	 * @return a mapping of message classes to their identifiers
	 */
	public static Map<Class<? extends Message>, String> getMessages() {
		return Collections.unmodifiableMap(CLASS_ID_MAP);
	}
	
}
