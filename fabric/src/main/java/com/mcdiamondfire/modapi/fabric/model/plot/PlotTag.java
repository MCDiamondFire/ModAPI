package com.mcdiamondfire.modapi.fabric.model.plot;

/**
 * A plot tag.
 */
public enum PlotTag {
	/// One or more short, fast minigames.
	ARCADE,
	/// Competitive games between players.
	VERSUS,
	/// Games focused on fighting enemies.
	COMBAT,
	/// Platforming challenges.
	PARKOUR,
	/// Games with places or stories to explore.
	ADVENTURE,
	/// Immersive settings intended for roleplay.
	ROLEPLAY,
	/// Games focused on planning and strategy.
	STRATEGY,
	/// Logic and problem-solving games.
	PUZZLE,
	/// Games based on answering questions.
	TRIVIA,
	/// Games in which players collect resources to progress.
	RESOURCES,
	/// Games about being the last player standing.
	ELIMINATION,
	/// Experiences centered on player creativity.
	CREATION,
	/// Plots which do not fit another category.
	MISCELLANEOUS,
	/// A tag introduced by a newer protocol version.
	UNKNOWN
}
