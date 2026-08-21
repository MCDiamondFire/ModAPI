package com.mcdiamondfire.modapi.fabric.client;

import org.jetbrains.annotations.ApiStatus;

/**
 * Indicates that the server rejected a ModAPI handshake.
 */
public final class HandshakeException extends RuntimeException {
	
	private final Error error;
	
	@ApiStatus.Internal
	public HandshakeException(Error error) {
		super("ModAPI handshake failed: " + error);
		this.error = error;
	}
	
	/**
	 * Returns why the handshake was rejected.
	 *
	 * @return the rejection reason
	 */
	public Error getError() {
		return error;
	}
	
	/**
	 * A handshake rejection reason.
	 */
	public enum Error {
		/// The supplied protocol version is not a valid semantic-version string.
		INVALID_PROTOCOL,
		/// The client protocol is incompatible with the server protocol.
		INCOMPATIBLE_PROTOCOL,
		/// This connection already completed the ModAPI handshake.
		ALREADY_CONNECTED,
		/// The request arrived after the configuration stage ended.
		NOT_APPLICABLE,
		/// A reason introduced by a newer protocol version.
		UNKNOWN
	}
	
}
