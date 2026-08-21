package com.mcdiamondfire.modapi.fabric.model;

/**
 * A position and rotation in the world.
 *
 * @param x     the X coordinate
 * @param y     the Y coordinate
 * @param z     the Z coordinate
 * @param pitch the pitch angle
 * @param yaw   the yaw angle
 */
public record Location(double x, double y, double z, float pitch, float yaw) {
	
	/**
	 * Creates a location without rotation.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param z the Z coordinate
	 */
	public Location(double x, double y, double z) {
		this(x, y, z, 0, 0);
	}
	
}
