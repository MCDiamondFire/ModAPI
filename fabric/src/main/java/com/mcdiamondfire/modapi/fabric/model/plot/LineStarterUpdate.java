package com.mcdiamondfire.modapi.fabric.model.plot;

/**
 * A change to a line starter in the current plot.
 *
 * @param lineStarter the affected line starter
 * @param action      the kind of change
 */
public record LineStarterUpdate(CodeLineStarter lineStarter, Action action) {
	
	/**
	 * The kind of line starter change.
	 */
	public enum Action {
		/// A line starter was added.
		ADD,
		/// A line starter changed.
		CHANGE,
		/// A line starter was removed.
		REMOVE,
		/// An action introduced by a newer protocol version.
		UNKNOWN
	}
	
}
