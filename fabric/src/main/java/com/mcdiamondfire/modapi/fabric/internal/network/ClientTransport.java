package com.mcdiamondfire.modapi.fabric.internal.network;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.mcdiamondfire.modapi.ModAPICodec;
import com.mcdiamondfire.modapi.ModAPIMessage;
import com.mcdiamondfire.modapi.ModAPIProtocol;
import com.mcdiamondfire.modapi.fabric.client.HandshakeException;
import com.mcdiamondfire.modapi.fabric.internal.mapping.ModelMapper;
import com.mcdiamondfire.modapi.messages.clientbound.server.S2CHandshakeResponse;
import com.mcdiamondfire.modapi.messages.common.ServerInfo;
import com.mcdiamondfire.modapi.messages.serverbound.server.C2SHandshakeRequest;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@ApiStatus.Internal
public final class ClientTransport {
	
	private static final Logger LOGGER = LoggerFactory.getLogger("ModAPI");
	
	private static final Map<Class<? extends Message>, List<Consumer<? extends Message>>> MESSAGE_LISTENERS = new ConcurrentHashMap<>();
	
	private static @Nullable ServerInfo serverInfo;
	
	private ClientTransport() {
		throw new UnsupportedOperationException();
	}
	
	public static void initialize(LifecycleListener lifecycleListener) {
		PayloadTypeRegistry.playS2C().register(Payload.ID, Payload.CODEC);
		PayloadTypeRegistry.playC2S().register(Payload.ID, Payload.CODEC);
		PayloadTypeRegistry.configurationS2C().register(Payload.ID, Payload.CODEC);
		PayloadTypeRegistry.configurationC2S().register(Payload.ID, Payload.CODEC);
		
		//noinspection resource
		ClientPlayNetworking.registerGlobalReceiver(Payload.ID, (payload, context) ->
				context.client().execute(() -> receive(payload))
		);
		
		ClientConfigurationConnectionEvents.INIT.register((handler, client) -> resetConnection(lifecycleListener));
		ClientConfigurationConnectionEvents.START.register((handler, client) -> startHandshake(lifecycleListener));
		ClientConfigurationConnectionEvents.DISCONNECT.register((handler, client) -> resetConnection(lifecycleListener));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetConnection(lifecycleListener));
	}
	
	public static boolean isConnected() {
		return serverInfo != null;
	}
	
	public static Optional<ServerInfo> getServerInfo() {
		return Optional.ofNullable(serverInfo);
	}
	
	public static <T extends Message> CompletableFuture<T> sendRequest(Message request, Class<T> responseType) {
		if (!isConnected()) {
			return CompletableFuture.failedFuture(new IllegalStateException("ModAPI is not connected"));
		}
		
		return RequestManager.sendRequest(request, responseType, ClientPlayNetworking::send);
	}
	
	public static void send(Message message) {
		if (!isConnected()) {
			throw new IllegalStateException("ModAPI is not connected");
		}
		
		try {
			ClientPlayNetworking.send(new Payload(ModAPICodec.encode(message)));
		} catch (InvalidProtocolBufferException exception) {
			throw new IllegalArgumentException("Failed to serialize ModAPI message", exception);
		}
	}
	
	public static <T extends Message> void registerMessageListener(Class<T> messageType, Consumer<T> listener) {
		MESSAGE_LISTENERS.computeIfAbsent(messageType, ignored -> new CopyOnWriteArrayList<>()).add(listener);
	}
	
	private static void startHandshake(LifecycleListener lifecycleListener) {
		LOGGER.info("Starting ModAPI handshake");
		
		//noinspection resource
		ClientConfigurationNetworking.registerReceiver(Payload.ID, (payload, context) ->
				context.client().execute(() -> receive(payload))
		);
		
		PacketSender sender = ClientConfigurationNetworking.getSender();
		sender.sendPacket(new RegistrationPayload(
				RegistrationPayload.REGISTER,
				List.of(Payload.CHANNEL)
		));
		
		RequestManager.sendRequest(
				C2SHandshakeRequest.newBuilder().setProtocolVersion(ModAPIProtocol.VERSION.toString()).build(),
				S2CHandshakeResponse.class,
				sender::sendPacket
		).whenComplete((response, failure) -> Minecraft.getInstance().execute(() -> {
			if (failure != null) {
				lifecycleListener.handshakeFailed(failure);
				return;
			}
			
			if (!response.hasSuccess()) {
				lifecycleListener.handshakeFailed(
						new HandshakeException(ModelMapper.handshakeError(response.getError()))
				);
				return;
			}
			
			serverInfo = response.getSuccess().getServerInfo();
			lifecycleListener.ready(serverInfo);
		}));
	}
	
	private static void receive(Payload payload) {
		ModAPIMessage message;
		try {
			message = ModAPICodec.decode(payload.json());
		} catch (InvalidProtocolBufferException | RuntimeException exception) {
			LOGGER.error("Failed to deserialize ModAPI message", exception);
			return;
		}
		
		if (message.requestId() != null) {
			RequestManager.complete(message.requestId(), message.message());
			return;
		}
		
		dispatchMessage(message.message());
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Message> void dispatchMessage(T message) {
		List<Consumer<? extends Message>> listeners = MESSAGE_LISTENERS.get(message.getClass());
		if (listeners == null) {
			return;
		}
		
		for (Consumer<? extends Message> listener : listeners) {
			((Consumer<T>) listener).accept(message);
		}
	}
	
	private static void resetConnection(LifecycleListener lifecycleListener) {
		boolean wasConnected = serverInfo != null;
		serverInfo = null;
		RequestManager.failAll(new IllegalStateException("ModAPI connection closed"));
		
		if (wasConnected) {
			lifecycleListener.disconnected();
		}
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
