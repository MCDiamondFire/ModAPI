package com.mcdiamondfire.proto;

import com.google.gson.*;
import com.google.protobuf.*;
import com.google.protobuf.util.JsonFormat;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * Utility class for serializing ModAPI messages to JSON format.
 */
@SuppressWarnings("unused")
public final class ModAPIUtility {
	
	private static final JsonFormat.Parser JSON_PARSER = JsonFormat.parser().ignoringUnknownFields();
	
	private ModAPIUtility() {
		// Prevent instantiation.
	}
	
	/**
	 * Serializes a Protocol Message to a JSON string,
	 * including the packet ID for identification on the receiving end.
	 *
	 * @param message the Protocol Message to serialize
	 * @return the serialized JSON string with its packet ID included
	 * @throws InvalidProtocolBufferException if serialization fails
	 */
	public static String serializeMessage(final Message message) throws InvalidProtocolBufferException {
		return serializeMessage(message, null);
	}
	
	/**
	 * Serializes a Protocol Message to a JSON string,
	 * including the packet ID and a request ID to correlate requests and responses.
	 *
	 * @param message   the Protocol Message to serialize
	 * @param requestId the request ID to include in the serialized message
	 * @return the serialized JSON string with its packet ID and request ID included
	 * @throws InvalidProtocolBufferException if serialization fails
	 */
	public static String serializeMessage(final Message message, final @Nullable Integer requestId) throws InvalidProtocolBufferException {
		final JsonObject jsonObject = JsonParser.parseString(JsonFormat.printer().print(message)).getAsJsonObject();
		
		final String messageId = ModAPIMessages.getMessageId(message.getClass())
				.orElseThrow(() -> new IllegalArgumentException("Message class not registered: " + message.getClass().getName()));
		
		jsonObject.add("packet_id", new JsonPrimitive(messageId));
		if (requestId != null) {
			jsonObject.add("request_id", new JsonPrimitive(requestId));
		}
		
		return jsonObject.toString();
	}
	
	/**
	 * Deserializes a JSON string back into a {@link ModAPIMessage}.
	 *
	 * @param json the JSON string to deserialize
	 * @return the deserialized Protocol Message and its ID wrapped in a {@link ModAPIMessage}
	 * @throws InvalidProtocolBufferException if deserialization fails
	 * @throws JsonSyntaxException            if the JSON is not properly formatted
	 * @throws IllegalStateException          if the JSON does not contain a valid packet ID
	 * @throws RuntimeException               if reflection fails to create a new builder instance
	 * @throws ClassCastException             if the packet ID is not of the expected type
	 */
	public static ModAPIMessage deserializeMessage(final String json) throws InvalidProtocolBufferException {
		try {
			final JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
			final String packetId = jsonObject.get("packet_id").getAsString();
			final Integer requestId = jsonObject.has("request_id") ? jsonObject.get("request_id").getAsInt() : null;
			
			final Optional<Class<? extends Message>> clazz = ModAPIMessages.getMessageClass(packetId);
			if (clazz.isEmpty()) {
				throw new IllegalStateException("No message class registered for packet ID: " + packetId);
			}
			final Message.Builder builder = (Message.Builder) clazz.get().getMethod("newBuilder").invoke(null);
			JSON_PARSER.merge(json, builder);
			return new ModAPIMessage(packetId, builder.build(), requestId);
		} catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Failed to deserialize message.", e);
		}
	}
	
}
