package com.mcdiamondfire.modapi.fabric.model.plot;

import net.minecraft.core.BlockPos;

/**
 * An axis-aligned cuboid.
 *
 * @param min the minimum corner
 * @param max the maximum corner
 */
public record Region(BlockPos min, BlockPos max) {

}
