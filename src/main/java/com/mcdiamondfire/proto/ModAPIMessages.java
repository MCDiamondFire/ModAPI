package com.mcdiamondfire.proto;

import com.google.protobuf.Message;
import com.mcdiamondfire.proto.messages.clientbound.player.*;
import com.mcdiamondfire.proto.messages.clientbound.plot.*;
import com.mcdiamondfire.proto.messages.clientbound.server.*;
import com.mcdiamondfire.proto.messages.serverbound.player.*;

import java.util.*;

/**
 * Holds a mapping of ModAPI message classes to their string identifiers.
 */
public final class ModAPIMessages {
	
	private static final Map<Class<? extends Message>, String> CLASS_ID_MAP = new HashMap<>();
	private static final Map<String, Class<? extends Message>> ID_CLASS_MAP = new HashMap<>();
	
	static {
		
		// Client-bound.
		
		// Server.
		registerMessage(S2CServerInfo.class, "s2c_server_info");
		registerMessage(S2CServerBooster.class, "s2c_server_booster");
		
		// Plot.
		registerMessage(S2CPlotInfo.class, "s2c_plot_info");
		registerMessage(S2CCodeTemplate.class, "s2c_code_template");
		registerMessage(S2CPlaceTemplateResult.class, "s2c_place_template_result");
		
		// Player.
		registerMessage(S2CPlayerCurrency.class, "s2c_player_currency");
		registerMessage(S2CPlayerPermissions.class, "s2c_player_permissions");
		registerMessage(S2CPlayerSwitchMode.class, "s2c_player_switch_mode");
		registerMessage(S2CChestReference.class, "s2c_chest_reference");
		
		// Server-bound.
		
		// Plot.
		registerMessage(C2SCodeOperation.class, "c2s_code_operation");
		registerMessage(C2SMultiCodeOperations.class, "c2s_multi_code_operations");
		
		// Player.
		registerMessage(C2SPlayerTeleport.class, "c2s_player_teleport");
		
	}
	
	private ModAPIMessages() {
		// Prevent instantiation.
	}
	
	private static void registerMessage(final Class<? extends Message> clazz, final String id) {
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
	public static Optional<String> getMessageId(final Class<? extends Message> clazz) {
		return Optional.ofNullable(CLASS_ID_MAP.get(clazz));
	}
	
	/**
	 * Gets the ModAPI message class for a given identifier.
	 *
	 * @param id the identifier of the ModAPI message
	 * @return an Optional containing the ModAPI message class or empty if not registered
	 */
	public static Optional<Class<? extends Message>> getMessageClass(final String id) {
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
