package com.mcdiamondfire.modapi.fabric.internal.network;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.mcdiamondfire.modapi.ModAPICodec;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

final class RequestManager {

	private static final int REQUEST_TIMEOUT_SECONDS = 10;
	private static final AtomicInteger REQUEST_COUNTER = new AtomicInteger();
	private static final Map<Integer, PendingRequest<?>> PENDING_REQUESTS = new ConcurrentHashMap<>();

	private RequestManager() {
	}

	static <T extends Message> CompletableFuture<T> sendRequest(Message request, Class<T> responseType, Consumer<Payload> sender) {
		int requestId = REQUEST_COUNTER.incrementAndGet();
		CompletableFuture<T> future = new CompletableFuture<>();

		PendingRequest<T> pendingRequest = new PendingRequest<>(responseType, future);
		PENDING_REQUESTS.put(requestId, pendingRequest);

		future.orTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
				.whenComplete((response, throwable) ->
						PENDING_REQUESTS.remove(requestId, pendingRequest)
				);

		try {
			sender.accept(new Payload(ModAPICodec.encode(request, requestId)));
		} catch (InvalidProtocolBufferException | RuntimeException exception) {
			PENDING_REQUESTS.remove(requestId, pendingRequest);
			future.completeExceptionally(exception);
		}

		return future;
	}

	static void complete(int requestId, Message response) {
		PendingRequest<?> request = PENDING_REQUESTS.remove(requestId);
		if (request == null) {
			return;
		}

		request.complete(response);
	}

	static void failAll(Throwable failure) {
		PENDING_REQUESTS.values().forEach(request -> request.future().completeExceptionally(failure));
		PENDING_REQUESTS.clear();
	}

	private record PendingRequest<T extends Message>(Class<T> responseType, CompletableFuture<T> future) {

		private void complete(Message response) {
			if (!responseType.isInstance(response)) {
				future.completeExceptionally(new ClassCastException(
						"Expected " + responseType.getName() + " but received " + response.getClass().getName()
				));
				return;
			}

			future.complete(responseType.cast(response));
		}

	}

}
