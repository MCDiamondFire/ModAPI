package com.mcdiamondfire.modapi.fabric.internal.network;

import com.mcdiamondfire.modapi.messages.ClientboundResponse;
import com.mcdiamondfire.modapi.messages.ServerboundCommand;
import com.mcdiamondfire.modapi.messages.ServerboundFrame;
import com.mcdiamondfire.modapi.messages.ServerboundRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

final class ClientSession {
	
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
	
	private final Object lifecycleLock = new Object();
	private final Map<Long, PendingRequest> pendingRequests = new HashMap<>();
	
	private volatile Consumer<Payload> sender;
	private volatile boolean connected = true;
	
	private long requestCounter;
	
	ClientSession(Consumer<Payload> sender) {
		this.sender = sender;
	}
	
	void setSender(Consumer<Payload> sender) {
		this.sender = sender;
	}
	
	boolean isConnected() {
		return this.connected;
	}
	
	void sendCommand(ServerboundCommand command) {
		ServerboundFrame frame = ServerboundFrame.newBuilder()
				.setCommand(command)
				.build();
		sendIfConnected(Payload.of(frame));
	}
	
	CompletableFuture<ClientboundResponse> sendRequest(
			ServerboundRequest request,
			ClientboundResponse.PayloadCase expectedResponse
	) {
		PendingRequest pendingRequest = registerPendingRequest(expectedResponse);
		
		ServerboundRequest requestWithId = request.toBuilder()
				.setRequestId(pendingRequest.requestId())
				.build();
		ServerboundFrame frame = ServerboundFrame.newBuilder()
				.setRequest(requestWithId)
				.build();
		
		try {
			sendIfConnected(Payload.of(frame));
		} catch (RuntimeException exception) {
			if (removePendingRequest(pendingRequest)) {
				pendingRequest.future().completeExceptionally(exception);
			}
		}
		
		return pendingRequest.future();
	}
	
	void receive(ClientboundResponse response) {
		long requestId = response.getRequestId();
		if (requestId <= 0) {
			return;
		}
		
		PendingRequest pendingRequest;
		synchronized (this.lifecycleLock) {
			pendingRequest = this.pendingRequests.remove(requestId);
		}
		if (pendingRequest == null) {
			return;
		}
		
		ClientboundResponse.PayloadCase payloadCase = response.getPayloadCase();
		if (payloadCase != pendingRequest.expectedResponse() &&
				payloadCase != ClientboundResponse.PayloadCase.PROTOCOL_ERROR
		) {
			pendingRequest.future().completeExceptionally(new IllegalStateException(
					"Unexpected ModAPI response payload: " + payloadCase
			));
			return;
		}
		pendingRequest.future().complete(response);
	}
	
	void disconnect(Throwable failure) {
		List<PendingRequest> pending;
		synchronized (this.lifecycleLock) {
			this.connected = false;
			pending = List.copyOf(this.pendingRequests.values());
			this.pendingRequests.clear();
		}
		pending.forEach(request -> request.future().completeExceptionally(failure));
	}
	
	private PendingRequest registerPendingRequest(ClientboundResponse.PayloadCase expectedResponse) {
		CompletableFuture<ClientboundResponse> future = new CompletableFuture<>();
		
		synchronized (this.lifecycleLock) {
			assertConnected();
			while (true) {
				long requestId = nextRequestId();
				PendingRequest pendingRequest = new PendingRequest(requestId, expectedResponse, future);
				
				if (this.pendingRequests.putIfAbsent(requestId, pendingRequest) == null) {
					registerTimeout(pendingRequest);
					return pendingRequest;
				}
			}
		}
	}
	
	private void sendIfConnected(Payload payload) {
		synchronized (this.lifecycleLock) {
			assertConnected();
		}
		this.sender.accept(payload);
	}
	
	private boolean removePendingRequest(PendingRequest pendingRequest) {
		synchronized (this.lifecycleLock) {
			return this.pendingRequests.remove(pendingRequest.requestId(), pendingRequest);
		}
	}
	
	private long nextRequestId() {
		// Better safe than sorry.
		if (this.requestCounter == Long.MAX_VALUE) {
			this.requestCounter = 1L;
		} else {
			this.requestCounter++;
		}
		
		return this.requestCounter;
	}
	
	private void registerTimeout(PendingRequest pendingRequest) {
		pendingRequest.future()
				.orTimeout(REQUEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
				.whenComplete((_, _) -> removePendingRequest(pendingRequest));
	}
	
	private void assertConnected() {
		if (!this.connected) {
			throw new IllegalStateException("ModAPI connection is closed");
		}
	}
	
	private record PendingRequest(
			long requestId,
			ClientboundResponse.PayloadCase expectedResponse,
			CompletableFuture<ClientboundResponse> future
	) {
	
	}
	
}
