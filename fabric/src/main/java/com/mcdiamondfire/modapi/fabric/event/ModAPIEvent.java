package com.mcdiamondfire.modapi.fabric.event;

import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * An event carrying one immutable ModAPI value.
 *
 * @param <T> the event value type
 */
public final class ModAPIEvent<T> {

	private final CopyOnWriteArrayList<Consumer<? super T>> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Registers a listener.
	 *
	 * @param listener the listener to invoke
	 * @return a registration that can be used to remove this listener
	 */
	public Registration register(Consumer<? super T> listener) {
		Objects.requireNonNull(listener, "listener");
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	@ApiStatus.Internal
	public void fire(T value) {
		for (Consumer<? super T> listener : listeners) {
			listener.accept(value);
		}
	}

}
