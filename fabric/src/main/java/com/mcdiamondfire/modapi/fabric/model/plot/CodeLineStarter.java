package com.mcdiamondfire.modapi.fabric.model.plot;

import com.mcdiamondfire.modapi.fabric.model.player.ChestReference;
import net.minecraft.core.BlockPos;

import java.util.Optional;

/**
 * A line starter in the current plot's code space.
 *
 * @param location the line starter's block location
 * @param chest    the action metadata, when available
 */
public record CodeLineStarter(BlockPos location, Optional<ChestReference> chest) {

}
