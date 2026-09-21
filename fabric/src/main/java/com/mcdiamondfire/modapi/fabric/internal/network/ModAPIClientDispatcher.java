package com.mcdiamondfire.modapi.fabric.internal.network;

import com.mcdiamondfire.modapi.fabric.client.ModAPI;
import com.mcdiamondfire.modapi.fabric.internal.mapping.ModelMapper;
import com.mcdiamondfire.modapi.messages.ClientboundEvent;
import com.mcdiamondfire.modapi.messages.ClientboundFrame;
import net.minecraft.network.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatches client-bound ModAPI frames to request sessions and event listeners.
 */
final class ModAPIClientDispatcher {
	
	private static final Logger LOGGER = LoggerFactory.getLogger("ModAPI");
	
	private ModAPIClientDispatcher() {
		throw new UnsupportedOperationException();
	}
	
	static void dispatch(ClientConnection source, Connection connection, ClientboundFrame frame) {
		switch (frame.getFrameCase()) {
			case RESPONSE -> source.receive(connection, frame.getResponse());
			case EVENT -> dispatchEvent(frame.getEvent());
			case FRAME_NOT_SET -> LOGGER.warn("Received a client-bound frame without a response or event");
		}
	}
	
	private static void dispatchEvent(ClientboundEvent event) {
		switch (event.getPayloadCase()) {
			case PLAYER_INFO -> ModAPI.ON_PLAYER_JOIN.fire(ModelMapper.playerInfo(event.getPlayerInfo()));
			case SERVER_BOOSTER -> ModAPI.ON_BOOSTER_UPDATE.fire(ModelMapper.serverBooster(event.getServerBooster()));
			case PLOT_INFO -> ModAPI.ON_JOIN_PLOT.fire(ModelMapper.plotInfo(event.getPlotInfo()));
			case PLOT_LINE_STARTERS ->
					ModAPI.ON_LINE_STARTER_SEND.fire(ModelMapper.lineStarters(event.getPlotLineStarters()));
			case PLOT_LINE_STARTER_UPDATE ->
					ModAPI.ON_LINE_STARTER_UPDATE.fire(ModelMapper.lineStarterUpdate(event.getPlotLineStarterUpdate()));
			case PLOT_PROFILING -> ModAPI.ON_PLOT_PROFILE.fire(event.getPlotProfiling().getCpuUsage());
			case PLAYER_SWITCH_MODE ->
					ModAPI.ON_MODE_SWITCH.fire(ModelMapper.playerMode(event.getPlayerSwitchMode().getMode()));
			case ACTION_REFERENCE ->
					ModAPI.ON_CODE_CHEST_OPEN.fire(ModelMapper.actionReference(event.getActionReference().getReference()));
			case PAYLOAD_NOT_SET -> LOGGER.warn("Received a client-bound event without a payload");
		}
	}
	
}
