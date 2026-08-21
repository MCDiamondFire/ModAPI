package com.mcdiamondfire.modapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for serializing ModAPI messages to JSON format.
 */
public final class ModAPICodec {
	
	private static final JsonFormat.Parser JSON_PARSER = JsonFormat.parser().ignoringUnknownFields();
	
	private ModAPICodec() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Serializes a Protocol Message to a JSON string,
	 * including the packet ID for identification on the receiving end.
	 *
	 * @param message the Protocol Message to serialize
	 * @return the serialized JSON string with its packet ID included
	 * @throws InvalidProtocolBufferException if serialization fails
	 */
	public static String encode(Message message) throws InvalidProtocolBufferException {
		return encode(message, null);
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
	public static String encode(Message message, @Nullable Integer requestId) throws InvalidProtocolBufferException {
		Objects.requireNonNull(message, "Message cannot be null");
		
		JsonObject jsonObject = JsonParser.parseString(JsonFormat.printer().print(message)).getAsJsonObject();
		
		String messageId = MessageRegistry.getId(message.getClass())
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
	public static ModAPIMessage decode(String json) throws InvalidProtocolBufferException {
		Objects.requireNonNull(json, "JSON cannot be null");
		
		try {
			JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
			String packetId = jsonObject.get("packet_id").getAsString();
			Integer requestId = jsonObject.has("request_id") ? jsonObject.get("request_id").getAsInt() : null;
			
			Optional<Class<? extends Message>> clazz = MessageRegistry.getType(packetId);
			if (clazz.isEmpty()) {
				throw new IllegalStateException("No message class registered for packet ID: " + packetId);
			}
			Message.Builder builder = (Message.Builder) clazz.get().getMethod("newBuilder").invoke(null);
			JSON_PARSER.merge(json, builder);
			return new ModAPIMessage(packetId, builder.build(), requestId);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Failed to deserialize message.", e);
		}
	}
	
}
