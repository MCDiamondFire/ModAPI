package com.mcdiamondfire.proto;

import com.google.gson.*;
import com.google.protobuf.*;
import com.google.protobuf.util.JsonFormat;

import java.lang.reflect.InvocationTargetException;

/**
 * Utility class for serializing plugin messages to JSON format.
 *
 * @since 1.0.0
 */
public final class PluginMessageUtility {
	
	private PluginMessageUtility() {
		// Prevent instantiation.
	}
	
	/**
	 * Serializes a protobuf message to a JSON string,
	 * including the packet ID for identification on the receiving end.
	 *
	 * @param message the protobuf message to serialize
	 * @return the serialized JSON string with packet ID
	 * @throws InvalidProtocolBufferException if serialization fails
	 */
	public static String serializeMessage(final Message message) throws InvalidProtocolBufferException {
		final String json = JsonFormat.printer().print(message);
		final JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
		jsonObject.add("packet_id", new JsonPrimitive(PluginMessages.getMessageId(message.getClass())));
		return jsonObject.toString();
	}
	
	/**
	 * Deserializes a JSON string back into a protobuf message of the specified class.
	 *
	 * @param json  the JSON string to deserialize
	 * @param clazz the class of the protobuf message to deserialize into
	 * @param <T>   the type of the protobuf message
	 * @return the deserialized protobuf message
	 * @throws InvalidProtocolBufferException if deserialization fails
	 * @throws JsonSyntaxException            if the JSON is not properly formatted
	 * @throws IllegalStateException          if the JSON does not contain a valid packet ID
	 * @throws RuntimeException               if reflection fails
	 * @throws ClassCastException             if the built message is not of the expected type
	 */
	public static <T extends Message> T deserializeMessage(final String json, final Class<T> clazz) throws InvalidProtocolBufferException {
		try {
			final JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
			jsonObject.remove("packet_id").getAsString();
			final String cleanedJson = jsonObject.toString();
			
			final Message.Builder builder = (Message.Builder) clazz.getMethod("newBuilder").invoke(null);
			JsonFormat.parser().merge(cleanedJson, builder);
			//noinspection unchecked -- on the caller to ensure the type is correct.
			return (T) builder.build();
		} catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Failed to deserialize message.", e);
		}
	}
	
}
