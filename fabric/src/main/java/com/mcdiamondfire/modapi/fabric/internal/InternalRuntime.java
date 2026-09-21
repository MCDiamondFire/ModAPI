package com.mcdiamondfire.modapi.fabric.internal;

import com.mcdiamondfire.modapi.fabric.client.ModAPI;
import com.mcdiamondfire.modapi.fabric.code.CodeOperation;
import com.mcdiamondfire.modapi.fabric.code.CodeOperationResult;
import com.mcdiamondfire.modapi.fabric.internal.mapping.CodeMapper;
import com.mcdiamondfire.modapi.fabric.internal.mapping.ModelMapper;
import com.mcdiamondfire.modapi.fabric.internal.network.ClientTransport;
import com.mcdiamondfire.modapi.fabric.model.Location;
import com.mcdiamondfire.modapi.fabric.model.server.ServerInfo;
import com.mcdiamondfire.modapi.messages.ClientboundResponse;
import com.mcdiamondfire.modapi.messages.ServerboundCommand;
import com.mcdiamondfire.modapi.messages.ServerboundRequest;
import com.mcdiamondfire.modapi.messages.clientbound.plot.S2CCodeOperationResult;
import com.mcdiamondfire.modapi.messages.clientbound.plot.S2CMultiCodeOperationsResult;
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
		ClientTransport.initialize(createLifecycleListener());
	}
	
	public static boolean isConnected() {
		return ClientTransport.isConnected();
	}
	
	public static Optional<ServerInfo> getServerInfo() {
		return ClientTransport.getServerInfo().map(ModelMapper::serverInfo);
	}
	
	public static void teleport(Location location) {
		ClientTransport.sendCommand(ServerboundCommand.newBuilder()
				.setPlayerTeleport(C2SPlayerTeleport.newBuilder()
						.setLocation(ModelMapper.location(location))
						.build())
				.build());
	}
	
	public static CompletableFuture<CodeOperationResult> execute(CodeOperation operation) {
		ServerboundRequest request = ServerboundRequest.newBuilder()
				.setCodeOperation(CodeMapper.operation(operation))
				.build();
		return ClientTransport.sendRequest(request, ClientboundResponse.PayloadCase.CODE_OPERATION_RESULT)
				.thenApply(InternalRuntime::codeOperationResult)
				.thenApply(CodeMapper::result);
	}
	
	public static CompletableFuture<List<CodeOperationResult>> execute(List<? extends CodeOperation> operations) {
		C2SMultiCodeOperations request = C2SMultiCodeOperations.newBuilder()
				.addAllOperations(operations.stream().map(CodeMapper::operation).toList())
				.build();
		ServerboundRequest serverboundRequest = ServerboundRequest.newBuilder()
				.setMultiCodeOperations(request)
				.build();
		return ClientTransport.sendRequest(
						serverboundRequest,
						ClientboundResponse.PayloadCase.MULTI_CODE_OPERATIONS_RESULT
				)
				.thenApply(InternalRuntime::multiCodeOperationResult)
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
	
	private static S2CCodeOperationResult codeOperationResult(ClientboundResponse response) {
		if (response.hasCodeOperationResult()) {
			return response.getCodeOperationResult();
		}
		throw unexpectedResponse(response);
	}
	
	private static S2CMultiCodeOperationsResult multiCodeOperationResult(ClientboundResponse response) {
		if (response.hasMultiCodeOperationsResult()) {
			return response.getMultiCodeOperationsResult();
		}
		throw unexpectedResponse(response);
	}
	
	private static IllegalStateException unexpectedResponse(ClientboundResponse response) {
		if (response.hasProtocolError()) {
			return new IllegalStateException("ModAPI protocol error: " + response.getProtocolError().getCode());
		}
		return new IllegalStateException("Unexpected ModAPI response payload: " + response.getPayloadCase());
	}
	
}
