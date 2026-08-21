package com.mcdiamondfire.modapi;

/**
 * Protocol constants.
 */
public final class ModAPIProtocol {
	
	public static final String CHANNEL = "hypercube:pm";
	public static final Semver VERSION = new Semver(1, 0, 0);
	
	private ModAPIProtocol() {
		throw new UnsupportedOperationException();
	}
	
}
