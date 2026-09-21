package com.mcdiamondfire.modapi;

/**
 * ModAPI protocol constants.
 */
public final class ModAPIProtocol {
	
	/**
	 * The plugin messaging channel used for ModAPI traffic.
	 */
	public static final String CHANNEL = "hypercube:pm";
	/**
	 * The current version of the protocol known to the API consumer.
	 */
	public static final Semver VERSION = new Semver(1, 0, 0);
	
	private ModAPIProtocol() {
		throw new UnsupportedOperationException();
	}
	
}
