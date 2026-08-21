package com.mcdiamondfire.modapi.fabric.model.player;

/**
 * A player's mode.
 */
public enum PlayerMode {
	/// Playing a plot.
	PLAY,
	/// Building a plot.
	BUILD,
	/// Editing a plot's code.
	DEV,
	/// Spectating a plot's code.
	CODE_SPECTATE,
	/// Completing an account verification check.
	VERIFY,
	/// Vanished.
	VANISH,
	/// At spawn.
	SPAWN,
	/// A mode introduced by a newer protocol version.
	UNKNOWN
}
