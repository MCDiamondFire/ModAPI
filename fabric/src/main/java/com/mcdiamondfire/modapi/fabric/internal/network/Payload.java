package com.mcdiamondfire.modapi.fabric.internal.network;

import com.google.protobuf.MessageLite;
import com.mcdiamondfire.modapi.ModAPIProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Arrays;

record Payload(byte[] data) implements CustomPacketPayload {
	
	Payload(byte[] data) {
		this.data = Arrays.copyOf(data, data.length);
	}
	
	public static final Identifier CHANNEL = Identifier.parse(ModAPIProtocol.CHANNEL);
	public static final Type<Payload> ID = new Type<>(CHANNEL);
	public static final StreamCodec<FriendlyByteBuf, Payload> CODEC = StreamCodec.of(
			Payload::write,
			Payload::read
	);
	
	static Payload of(MessageLite message) {
		return new Payload(message.toByteArray());
	}
	
	private static Payload read(FriendlyByteBuf buffer) {
		byte[] data = new byte[buffer.readableBytes()];
		buffer.readBytes(data);
		return new Payload(data);
	}
	
	private static void write(FriendlyByteBuf buffer, Payload payload) {
		buffer.writeBytes(payload.data());
	}
	
	@Override
	public byte[] data() {
		return Arrays.copyOf(this.data, this.data.length);
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
	
}
