package com.mcdiamondfire.modapi.fabric.internal.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;

import java.util.List;

final class ComponentMapper {
	
	private ComponentMapper() {
	}
	
	static Component component(String json) {
		if (json.isEmpty()) {
			return Component.empty();
		}
		
		try {
			JsonElement element = JsonParser.parseString(json);
			return ComponentSerialization.CODEC.parse(jsonOps(), element).getOrThrow(message ->
					new IllegalArgumentException("Invalid Minecraft component: " + message)
			);
		} catch (JsonParseException exception) {
			throw new IllegalArgumentException("Invalid Minecraft component JSON", exception);
		}
	}
	
	static List<Component> components(List<String> json) {
		return json.stream().map(ComponentMapper::component).toList();
	}
	
	private static DynamicOps<JsonElement> jsonOps() {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection == null) {
			return JsonOps.INSTANCE;
		}
		return RegistryOps.create(JsonOps.INSTANCE, connection.registryAccess());
	}
	
}
