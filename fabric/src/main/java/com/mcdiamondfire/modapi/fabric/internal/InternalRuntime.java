package com.mcdiamondfire.modapi.fabric.internal;

import com.mcdiamondfire.modapi.fabric.client.ModAPI;
import com.mcdiamondfire.modapi.fabric.code.CodeOperation;
import com.mcdiamondfire.modapi.fabric.code.CodeOperationResult;
import com.mcdiamondfire.modapi.fabric.internal.mapping.CodeMapper;
import com.mcdiamondfire.modapi.fabric.internal.mapping.ModelMapper;
import com.mcdiamondfire.modapi.fabric.internal.network.ClientTransport;
import com.mcdiamondfire.modapi.fabric.model.Location;
import com.mcdiamondfire.modapi.fabric.model.server.ServerInfo;
import com.mcdiamondfire.modapi.messages.clientbound.player.S2CChestReference;
import com.mcdiamondfire.modapi.messages.clientbound.player.S2CPlayerSwitchMode;
import com.mcdiamondfire.modapi.messages.clientbound.plot.*;
import com.mcdiamondfire.modapi.messages.clientbound.server.S2CPlayerInfo;
import com.mcdiamondfire.modapi.messages.clientbound.server.S2CServerBooster;
import com.mcdiamondfire.modapi.messages.serverbound.player.C2SPlayerTeleport;
import com.mcdiamondfire.modapi.messages.serverbound.plot.C2SMultiCodeOperations;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public final class InternalRuntime {
	
	private InternalRuntime() {
		throw new UnsupportedOperationException();
	}
	
	public static void initialize() {
		registerMessages();
		ClientTransport.initialize(createLifecycleListener());
	}
	
	public static boolean isConnected() {
		return ClientTransport.isConnected();
	}
	
	public static Optional<ServerInfo> getServerInfo() {
		return ClientTransport.getServerInfo().map(ModelMapper::serverInfo);
	}
	
	public static void teleport(Location location) {
		ClientTransport.send(C2SPlayerTeleport.newBuilder()
				.setLocation(ModelMapper.location(location))
				.build());
	}
	
	public static CompletableFuture<CodeOperationResult> execute(CodeOperation operation) {
		return ClientTransport.sendRequest(
				CodeMapper.operation(operation),
				S2CCodeOperationResult.class
		).thenApply(CodeMapper::result);
	}
	
	public static CompletableFuture<List<CodeOperationResult>> execute(List<? extends CodeOperation> operations) {
		C2SMultiCodeOperations request = C2SMultiCodeOperations.newBuilder()
				.addAllOperations(operations.stream().map(CodeMapper::operation).toList())
				.build();
		return ClientTransport.sendRequest(request, S2CMultiCodeOperationsResult.class)
				.thenApply(response -> response.getResultsList().stream().map(CodeMapper::result).toList());
	}
	
	private static ClientTransport.LifecycleListener createLifecycleListener() {
		return new ClientTransport.LifecycleListener() {
			@Override
			public void ready(com.mcdiamondfire.modapi.messages.common.ServerInfo protocolServerInfo) {
				ModAPI.ON_CONNECT.fire(ModelMapper.serverInfo(protocolServerInfo));
			}
			
			@Override
			public void handshakeFailed(Throwable failure) {
				ModAPI.ON_CONNECTION_FAILED.fire(failure);
			}
			
			@Override
			public void disconnected() {
				ModAPI.ON_DISCONNECT.fire();
			}
		};
	}
	
	private static void registerMessages() {
		ClientTransport.registerMessageListener(S2CPlayerInfo.class, message ->
				ModAPI.ON_PLAYER_JOIN.fire(ModelMapper.playerInfo(message))
		);
		ClientTransport.registerMessageListener(S2CServerBooster.class, message ->
				ModAPI.ON_BOOSTER_UPDATE.fire(ModelMapper.serverBooster(message))
		);
		ClientTransport.registerMessageListener(S2CPlayerSwitchMode.class, message ->
				ModAPI.ON_MODE_SWITCH.fire(ModelMapper.playerMode(message.getMode()))
		);
		ClientTransport.registerMessageListener(S2CChestReference.class, message ->
				ModAPI.ON_CODE_CHEST_OPEN.fire(ModelMapper.chestReference(message.getReference()))
		);
		ClientTransport.registerMessageListener(S2CPlotInfo.class, message ->
				ModAPI.ON_JOIN_PLOT.fire(ModelMapper.plotInfo(message))
		);
		ClientTransport.registerMessageListener(S2CPlotLineStarters.class, message ->
				ModAPI.ON_LINE_STARTER_SEND.fire(
						message.getLineStarterList().stream().map(ModelMapper::lineStarter).toList()
				)
		);
		ClientTransport.registerMessageListener(S2CPlotLineStarterUpdate.class, message ->
				ModAPI.ON_LINE_STARTER_UPDATE.fire(ModelMapper.lineStarterUpdate(message))
		);
		ClientTransport.registerMessageListener(S2CPlotProfiling.class, message ->
				ModAPI.ON_PLOT_PROFILE.fire(message.getCpuUsage())
		);
	}
	
}
