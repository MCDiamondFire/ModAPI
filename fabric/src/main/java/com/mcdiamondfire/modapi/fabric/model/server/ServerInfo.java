package com.mcdiamondfire.modapi.fabric.model.server;

import com.mcdiamondfire.modapi.Semver;

/**
 * Information advertised by the connected server.
 *
 * @param protocolVersion the ModAPI protocol version
 * @param name            the server identifier used by the proxy
 * @param patchVersion    the patch running on the server
 * @param type            the kind of server
 */
public record ServerInfo(Semver protocolVersion, String name, String patchVersion, ServerType type) {

	/**
	 * A server type.
	 */
	public enum ServerType {
		/// A main node.
		MAIN,
		/// The beta node.
		BETA,
		/// A development node.
		DEV,
		/// A public testing node.
		PUBLIC_TEST,
		/// An event node.
		EVENT,
		/// A locally hosted development server.
		LOCAL,
		/// A private node.
		PRIVATE,
		/// A server type introduced by a newer protocol version.
		UNKNOWN
	}

}
