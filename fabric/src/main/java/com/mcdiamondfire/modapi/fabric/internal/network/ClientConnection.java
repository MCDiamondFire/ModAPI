package com.mcdiamondfire.modapi.fabric.internal.network;

import com.mcdiamondfire.modapi.messages.ClientboundResponse;
import com.mcdiamondfire.modapi.messages.ServerboundCommand;
import com.mcdiamondfire.modapi.messages.ServerboundRequest;
import com.mcdiamondfire.modapi.messages.common.ServerInfo;
import net.minecraft.network.Connection;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

final class ClientConnection {
	
	private final Connection connection;
	private final ClientSession session;
	private final @Nullable ServerInfo serverInfo;
	
	private ClientConnection(Connection connection, ClientSession session, @Nullable ServerInfo serverInfo) {
		this.connection = connection;
		this.session = session;
		this.serverInfo = serverInfo;
	}
	
	static ClientConnection connecting(Connection connection, Consumer<Payload> sender) {
		return new ClientConnection(connection, new ClientSession(sender), null);
	}
	
	ClientConnection ready(ServerInfo serverInfo, Consumer<Payload> sender) {
		this.session.setSender(sender);
		return new ClientConnection(this.connection, this.session, serverInfo);
	}
	
	CompletableFuture<ClientboundResponse> sendHandshakeRequest(ServerboundRequest request) {
		if (this.isReady()) {
			return CompletableFuture.failedFuture(new IllegalStateException("ModAPI connection is already ready"));
		}
		
		// Send the request and expect a handshake response.
		return this.session.sendRequest(request, ClientboundResponse.PayloadCase.HANDSHAKE_RESPONSE);
	}
	
	CompletableFuture<ClientboundResponse> sendRequest(
			ServerboundRequest request,
			ClientboundResponse.PayloadCase expectedResponse) {
		if (!this.isReady()) {
			return CompletableFuture.failedFuture(new IllegalStateException("ModAPI connection is not ready"));
		}
		
		return this.session.sendRequest(request, expectedResponse);
	}
	
	void sendCommand(ServerboundCommand command) {
		assertReady();
		this.session.sendCommand(command);
	}
	
	boolean isReady() {
		return this.serverInfo != null && this.session.isConnected();
	}
	
	@Nullable ServerInfo serverInfo() {
		return this.serverInfo;
	}
	
	boolean rejects(Connection connection) {
		return this.connection != connection;
	}
	
	void receive(Connection connection, ClientboundResponse response) {
		if (this.rejects(connection)) {
			return;
		}
		this.session.receive(response);
	}
	
	void disconnect(Throwable failure) {
		this.session.disconnect(failure);
	}
	
	private void assertReady() {
		if (!this.isReady()) {
			throw new IllegalStateException("ModAPI connection is not ready");
		}
	}
	
}
