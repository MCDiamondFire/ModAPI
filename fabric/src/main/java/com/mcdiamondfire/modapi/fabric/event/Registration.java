package com.mcdiamondfire.modapi.fabric.event;

/**
 * A removable event registration.
 */
@FunctionalInterface
public interface Registration {
	
	/**
	 * Unregisters the listener.
	 */
	void unregister();
	
}
