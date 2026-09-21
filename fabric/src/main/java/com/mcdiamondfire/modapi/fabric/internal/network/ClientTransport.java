package com.mcdiamondfire.modapi.fabric.internal.network;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mcdiamondfire.modapi.ModAPIProtocol;
import com.mcdiamondfire.modapi.fabric.client.HandshakeException;
import com.mcdiamondfire.modapi.fabric.internal.mapping.ModelMapper;
import com.mcdiamondfire.modapi.messages.ClientboundFrame;
import com.mcdiamondfire.modapi.messages.ClientboundResponse;
import com.mcdiamondfire.modapi.messages.ServerboundCommand;
import com.mcdiamondfire.modapi.messages.ServerboundRequest;
import com.mcdiamondfire.modapi.messages.common.ServerInfo;
import com.mcdiamondfire.modapi.messages.serverbound.server.C2SHandshakeRequest;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@ApiStatus.Internal
public final class ClientTransport {
	
	private static final Logger LOGGER = LoggerFactory.getLogger("ModAPI");
	
	private static final AtomicReference<@Nullable ClientConnection> ACTIVE_CONNECTION = new AtomicReference<>();
	
	private ClientTransport() {
		throw new UnsupportedOperationException();
	}
	
	public static void initialize(LifecycleListener lifecycleListener) {
		PayloadTypeRegistry.clientboundPlay().register(Payload.ID, Payload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(Payload.ID, Payload.CODEC);
		PayloadTypeRegistry.clientboundConfiguration().register(Payload.ID, Payload.CODEC);
		PayloadTypeRegistry.serverboundConfiguration().register(Payload.ID, Payload.CODEC);
		
		ClientPlayNetworking.registerGlobalReceiver(Payload.ID, (payload, context) -> {
			Connection connection = context.packetContext().orElseThrow(PacketContext.CONNECTION);
			//noinspection resource
			context.client().execute(() -> receive(connection, payload));
		});
		
		ClientConfigurationConnectionEvents.INIT.register((_, _) ->
				resetConnection(lifecycleListener)
		);
		ClientConfigurationConnectionEvents.START.register((listener, _) ->
				startHandshake(listener, lifecycleListener)
		);
		ClientConfigurationConnectionEvents.DISCONNECT.register((listener, _) ->
				resetConnection(connectionOf((PacketContextProvider) listener), lifecycleListener)
		);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, _) ->
				resetConnection(connectionOf((PacketContextProvider) handler), lifecycleListener)
		);
	}
	
	public static boolean isConnected() {
		ClientConnection currentConnection = ACTIVE_CONNECTION.get();
		return currentConnection != null && currentConnection.isReady();
	}
	
	public static Optional<ServerInfo> getServerInfo() {
		ClientConnection currentConnection = ACTIVE_CONNECTION.get();
		if (currentConnection == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(currentConnection.serverInfo());
	}
	
	public static CompletableFuture<ClientboundResponse> sendRequest(
			ServerboundRequest request,
			ClientboundResponse.PayloadCase expectedResponse
	) {
		ClientConnection currentConnection = ACTIVE_CONNECTION.get();
		if (currentConnection == null || !currentConnection.isReady()) {
			return CompletableFuture.failedFuture(new IllegalStateException("ModAPI is not connected"));
		}
		
		return currentConnection.sendRequest(request, expectedResponse);
	}
	
	public static void sendCommand(ServerboundCommand command) {
		ClientConnection currentConnection = ACTIVE_CONNECTION.get();
		if (currentConnection == null || !currentConnection.isReady()) {
			throw new IllegalStateException("ModAPI is not connected");
		}
		
		currentConnection.sendCommand(command);
	}
	
	private static void startHandshake(ClientConfigurationPacketListenerImpl listener, LifecycleListener lifecycleListener) {
		LOGGER.info("Starting ModAPI handshake");
		
		Connection connection = connectionOf((PacketContextProvider) listener);
		PacketSender sender = ClientConfigurationNetworking.getSender();
		ClientConnection newConnection = ClientConnection.connecting(connection, sender::sendPacket);
		ACTIVE_CONNECTION.set(newConnection);
		
		//noinspection resource
		ClientConfigurationNetworking.registerReceiver(Payload.ID, (payload, context) ->
				context.client().execute(() -> receive(connection, payload))
		);
		
		// Send a registration packet to fix an odd race condition.
		sender.sendPacket(new RegistrationPayload(
				RegistrationPayload.REGISTER,
				List.of(Payload.CHANNEL)
		));
		
		newConnection.sendHandshakeRequest(
				ServerboundRequest.newBuilder()
						.setHandshakeRequest(C2SHandshakeRequest.newBuilder()
								.setProtocolVersion(ModAPIProtocol.VERSION.toString())
								.build()
						)
						.build()
		).whenComplete((response, failure) -> Minecraft.getInstance().execute(() -> {
			if (ACTIVE_CONNECTION.get() != newConnection) {
				return;
			}
			
			if (failure != null) {
				lifecycleListener.handshakeFailed(failure);
				return;
			}
			
			if (response.hasProtocolError()) {
				lifecycleListener.handshakeFailed(new IllegalStateException(
						"Server rejected ModAPI handshake: " + response.getProtocolError().getCode()
				));
				return;
			}
			if (!response.hasHandshakeResponse()) {
				lifecycleListener.handshakeFailed(new IllegalStateException(
						"Server returned an unexpected ModAPI handshake response"
				));
				return;
			}
			
			if (!response.getHandshakeResponse().hasSuccess()) {
				lifecycleListener.handshakeFailed(
						new HandshakeException(ModelMapper.handshakeError(response.getHandshakeResponse().getError()))
				);
				return;
			}
			
			ServerInfo serverInfo = response.getHandshakeResponse().getSuccess().getServerInfo();
			ClientConnection readyConnection = newConnection.ready(serverInfo, ClientPlayNetworking::send);
			if (ACTIVE_CONNECTION.compareAndSet(newConnection, readyConnection)) {
				lifecycleListener.ready(serverInfo);
			}
		}));
	}
	
	private static void receive(Connection connection, Payload payload) {
		ClientConnection currentConnection = ACTIVE_CONNECTION.get();
		if (currentConnection == null || currentConnection.rejects(connection)) {
			return;
		}
		
		ClientboundFrame frame;
		try {
			frame = ClientboundFrame.parseFrom(payload.data());
		} catch (InvalidProtocolBufferException | RuntimeException exception) {
			LOGGER.error("Failed to deserialize ModAPI frame", exception);
			return;
		}
		
		if (ACTIVE_CONNECTION.get() != currentConnection || currentConnection.rejects(connection)) {
			return;
		}
		ModAPIClientDispatcher.dispatch(currentConnection, connection, frame);
	}
	
	private static void resetConnection(LifecycleListener lifecycleListener) {
		ClientConnection oldConnection = ACTIVE_CONNECTION.getAndSet(null);
		if (oldConnection == null) {
			return;
		}
		
		boolean wasReady = oldConnection.isReady();
		oldConnection.disconnect(new IllegalStateException("ModAPI connection closed"));
		if (wasReady) {
			lifecycleListener.disconnected();
		}
	}
	
	private static void resetConnection(Connection connection, LifecycleListener lifecycleListener) {
		ClientConnection currentConnection = ACTIVE_CONNECTION.get();
		if (currentConnection == null || currentConnection.rejects(connection)) {
			return;
		}
		if (!ACTIVE_CONNECTION.compareAndSet(currentConnection, null)) {
			return;
		}
		
		boolean wasReady = currentConnection.isReady();
		currentConnection.disconnect(new IllegalStateException("ModAPI connection closed"));
		if (wasReady) {
			lifecycleListener.disconnected();
		}
	}
	
	private static Connection connectionOf(PacketContextProvider provider) {
		return provider.getPacketContext().orElseThrow(PacketContext.CONNECTION);
	}
	
	public interface LifecycleListener {
		
		default void ready(ServerInfo serverInfo) {
		}
		
		default void handshakeFailed(Throwable failure) {
		}
		
		default void disconnected() {
		}
		
	}
	
}
