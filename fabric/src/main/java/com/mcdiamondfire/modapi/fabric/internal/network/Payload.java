package com.mcdiamondfire.modapi.fabric.internal.network;

import com.mcdiamondfire.modapi.ModAPIProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

record Payload(String json) implements CustomPacketPayload {
	
	public static final Identifier CHANNEL = Identifier.parse(ModAPIProtocol.CHANNEL);
	public static final Type<Payload> ID = new Type<>(CHANNEL);
	public static final StreamCodec<FriendlyByteBuf, Payload> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			Payload::json,
			Payload::new
	);
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
	
}
