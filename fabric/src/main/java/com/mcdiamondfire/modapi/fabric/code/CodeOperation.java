package com.mcdiamondfire.modapi.fabric.code;

import net.minecraft.core.BlockPos;

/**
 * A code space mutation or query that can be sent through {@code ModAPI}.
 */
public sealed interface CodeOperation permits
		CodeOperation.GetByLocation,
		CodeOperation.GetByBlock,
		CodeOperation.Place,
		CodeOperation.DeleteByLocation,
		CodeOperation.DeleteByBlock,
		CodeOperation.ReplaceByLocation,
		CodeOperation.ReplaceByBlock {
	
	/**
	 * Gets the block or complete line at a code space location.
	 *
	 * @param location the code space block location
	 */
	record GetByLocation(BlockPos location) implements CodeOperation {
	
	}
	
	/**
	 * Gets an existing line starter by its type and name.
	 *
	 * @param type the line starter type
	 * @param name the function or process name, or the event action name
	 */
	record GetByBlock(LineStarterType type, String name) implements CodeOperation {
	
	}
	
	/**
	 * Places a template at an unoccupied code space location.
	 *
	 * @param location the destination code space location
	 * @param template the code to place
	 */
	record Place(BlockPos location, CodeTemplate template) implements CodeOperation {
	
	}
	
	/**
	 * Deletes a code block and the remainder of its line. Selecting a line
	 * starter deletes the complete line.
	 *
	 * @param location the code space block location
	 */
	record DeleteByLocation(BlockPos location) implements CodeOperation {
	
	}
	
	/**
	 * Deletes an existing line starter by its type and name.
	 *
	 * @param type the line starter type
	 * @param name the function or process name, or the event action name
	 */
	record DeleteByBlock(LineStarterType type, String name) implements CodeOperation {
	
	}
	
	/**
	 * Replaces a code block and the remainder of its line. Selecting a line
	 * starter replaces the complete line.
	 *
	 * @param location the code space block location
	 * @param template the replacement code
	 */
	record ReplaceByLocation(BlockPos location, CodeTemplate template) implements CodeOperation {
	
	}
	
	/**
	 * Replaces an existing line starter by its type and name.
	 *
	 * @param type     the line starter type
	 * @param name     the function or process name, or the event action name
	 * @param template the replacement code
	 */
	record ReplaceByBlock(LineStarterType type, String name, CodeTemplate template) implements CodeOperation {
	
	}
	
}
