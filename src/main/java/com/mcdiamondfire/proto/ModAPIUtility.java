package com.mcdiamondfire.proto;

import com.google.gson.*;
import com.google.protobuf.*;
import com.google.protobuf.util.JsonFormat;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * Utility class for serializing ModAPI messages to JSON format.
 */
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
		final String json = JsonFormat.printer().print(message);
		final JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
		final Optional<String> messageId = ModAPIMessages.getMessageId(message.getClass());
		if (messageId.isEmpty()) {
			throw new IllegalArgumentException("Message class not registered: " + message.getClass().getName());
		}
		jsonObject.add("packet_id", new JsonPrimitive(messageId.get()));
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
			final String packetId = jsonObject.remove("packet_id").getAsString();
			final String cleanedJson = jsonObject.toString();
			
			final Optional<Class<? extends Message>> clazz = ModAPIMessages.getMessageClass(packetId);
			if (clazz.isEmpty()) {
				throw new IllegalStateException("No message class registered for packet ID: " + packetId);
			}
			final Message.Builder builder = (Message.Builder) clazz.get().getMethod("newBuilder").invoke(null);
			JSON_PARSER.merge(cleanedJson, builder);
			return new ModAPIMessage(packetId, builder.build());
		} catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Failed to deserialize message.", e);
		}
	}
	
}
