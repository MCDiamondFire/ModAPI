package com.mcdiamondfire.modapi.fabric.event;

import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An event with no associated value.
 */
public final class ModAPISignal {

	private final CopyOnWriteArrayList<Runnable> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Registers a listener.
	 *
	 * @param listener the listener to invoke
	 * @return a registration that can be used to remove this listener
	 */
	public Registration register(Runnable listener) {
		Objects.requireNonNull(listener, "listener");
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	@ApiStatus.Internal
	public void fire() {
		for (Runnable listener : listeners) {
			listener.run();
		}
	}

}
