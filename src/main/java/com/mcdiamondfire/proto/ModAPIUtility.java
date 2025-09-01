package com.mcdiamondfire.proto;

import com.google.gson.*;
import com.google.protobuf.*;
import com.google.protobuf.util.JsonFormat;

import java.lang.reflect.InvocationTargetException;

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
		jsonObject.add("packet_id", new JsonPrimitive(ModAPIMessages.getMessageId(message.getClass())));
		return jsonObject.toString();
	}
	
	/**
	 * Deserializes a JSON string back into a Protocol Message of the specified class.
	 *
	 * @param json  the JSON string to deserialize
	 * @param clazz the class of the Protocol Message to deserialize into
	 * @param <T>   the type of the Protocol Message
	 * @return the deserialized Protocol Message
	 * @throws InvalidProtocolBufferException if deserialization fails
	 * @throws JsonSyntaxException            if the JSON is not properly formatted
	 * @throws IllegalStateException          if the JSON does not contain a valid packet ID
	 * @throws RuntimeException               if reflection fails to create a new builder instance
	 * @throws ClassCastException             if the built message is not of the expected type
	 */
	public static <T extends Message> T deserializeMessage(final String json, final Class<T> clazz) throws InvalidProtocolBufferException {
		try {
			final JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
			jsonObject.remove("packet_id").getAsString();
			final String cleanedJson = jsonObject.toString();
			
			final Message.Builder builder = (Message.Builder) clazz.getMethod("newBuilder").invoke(null);
			JSON_PARSER.merge(cleanedJson, builder);
			//noinspection unchecked -- on the caller to ensure the type is correct.
			return (T) builder.build();
		} catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Failed to deserialize message.", e);
		}
	}
	
}
