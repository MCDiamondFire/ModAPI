package com.mcdiamondfire.modapi.fabric.model;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

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
	 * @return a new Location
	 */
	public static Location of(double x, double y, double z) {
		return new Location(x, y, z, 0, 0);
	}
	
	/**
	 * Creates a location from a {@link Vec3i} without rotation.
	 *
	 * @param position the position vector
	 * @return a new Location
	 */
	public static Location of(Vec3i position) {
		return new Location(position.getX(), position.getY(), position.getZ(), 0, 0);
	}
	
	
	/**
	 * Creates a location from a {@link Vec3i} with the given rotation.
	 *
	 * @param position the position vector
	 * @param pitch    the pitch angle
	 * @param yaw      the yaw angle
	 * @return a new Location
	 */
	public static Location of(Vec3i position, float pitch, float yaw) {
		return new Location(position.getX(), position.getY(), position.getZ(), pitch, yaw);
	}
	
	/**
	 * Creates a location from a {@link BlockPos} without rotation.
	 *
	 * @param position the block position
	 * @return a new Location
	 */
	public static Location of(BlockPos position) {
		return new Location(position.getX(), position.getY(), position.getZ(), 0, 0);
	}
	
	/**
	 * Creates a location from a {@link BlockPos} with rotation.
	 *
	 * @param position the block position
	 * @param pitch    the pitch angle
	 * @param yaw      the yaw angle
	 * @return a new Location
	 */
	public static Location of(BlockPos position, float pitch, float yaw) {
		return new Location(position.getX(), position.getY(), position.getZ(), pitch, yaw);
	}
	
}
