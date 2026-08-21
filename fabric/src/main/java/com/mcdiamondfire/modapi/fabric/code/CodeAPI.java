package com.mcdiamondfire.modapi.fabric.code;

import com.mcdiamondfire.modapi.fabric.client.ModAPI;
import com.mcdiamondfire.modapi.fabric.internal.InternalRuntime;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Operations for reading and modifying code.
 *
 * <p>Use the instance at {@link ModAPI#CODE}.</p>
 */
public final class CodeAPI {

	@ApiStatus.Internal
	public CodeAPI() {
	}

	/**
	 * Executes one code operation and returns its result.
	 *
	 * @param operation the operation to execute
	 * @return a future completed with the result
	 */
	public CompletableFuture<CodeOperationResult> execute(CodeOperation operation) {
		return InternalRuntime.execute(operation);
	}

	/**
	 * Executes several code operations in request order using one request.
	 *
	 * @param operations the operations to execute
	 * @return a future completed with one result per operation, in request order
	 */
	public CompletableFuture<List<CodeOperationResult>> execute(List<? extends CodeOperation> operations) {
		return InternalRuntime.execute(operations);
	}

	/**
	 * Gets template JSON for the block at a code space location. A line starter
	 * location returns the complete line. The player must have developer permissions.
	 *
	 * @param location the code space block location
	 * @return a future completed with template JSON
	 */
	public CompletableFuture<String> get(BlockPos location) {
		return requireTemplate(execute(new CodeOperation.GetByLocation(location)));
	}

	/**
	 * Gets template JSON for an existing line starter by type and name.
	 *
	 * @param type the line starter type
	 * @param name the function or process name, or the event action name
	 * @return a future completed with template JSON
	 */
	public CompletableFuture<String> get(LineStarterType type, String name) {
		return requireTemplate(execute(new CodeOperation.GetByBlock(type, name)));
	}

	/**
	 * Places a template at a code space location. The template must fit without
	 * overwriting existing blocks or leaving the plot's code space region.
	 *
	 * @param location the destination code space location
	 * @param template the code to place
	 * @return a future completed when the server accepts the operation
	 */
	public CompletableFuture<Void> place(BlockPos location, CodeTemplate template) {
		return requireResult(execute(new CodeOperation.Place(location, template)));
	}

	/**
	 * Deletes a code block and the remainder of its line. Selecting a line
	 * starter deletes the complete line.
	 *
	 * @param location the code space block location
	 * @return a future completed when the server accepts the operation
	 */
	public CompletableFuture<Void> delete(BlockPos location) {
		return requireResult(execute(new CodeOperation.DeleteByLocation(location)));
	}

	/**
	 * Deletes an existing line starter by type and name.
	 *
	 * @param type the line starter type
	 * @param name the function or process name, or the event action name
	 * @return a future completed when the server accepts the operation
	 */
	public CompletableFuture<Void> delete(LineStarterType type, String name) {
		return requireResult(execute(new CodeOperation.DeleteByBlock(type, name)));
	}

	/**
	 * Replaces a code block and the remainder of its line. Selecting a line
	 * starter replaces the complete line. The new code may occupy the
	 * replaced blocks but may not collide with other code or leave the code space region.
	 *
	 * @param location the code space block location
	 * @param template the replacement code
	 * @return a future completed when the server accepts the operation
	 */
	public CompletableFuture<Void> replace(BlockPos location, CodeTemplate template) {
		return requireResult(execute(new CodeOperation.ReplaceByLocation(location, template)));
	}

	/**
	 * Replaces an existing line starter selected by type and name.
	 *
	 * @param type the line starter type
	 * @param name the function or process name, or the event action name
	 * @param template the replacement code
	 * @return a future completed when the server accepts the operation
	 */
	public CompletableFuture<Void> replace(LineStarterType type, String name, CodeTemplate template) {
		return requireResult(execute(new CodeOperation.ReplaceByBlock(type, name, template)));
	}

	private CompletableFuture<Void> requireResult(CompletableFuture<CodeOperationResult> resultFuture) {
		return resultFuture.thenAccept(result -> {
			// Failed results should cause the future to complete exceptionally.
			if (result instanceof CodeOperationResult.Failure(CodeOperationResult.Error error)) {
				throw new CodeOperationException(error);
			}
		});
	}

	private CompletableFuture<String> requireTemplate(CompletableFuture<CodeOperationResult> resultFuture) {
		return resultFuture.thenApply(result -> {
			if (result instanceof CodeOperationResult.Failure(CodeOperationResult.Error error)) {
				throw new CodeOperationException(error);
			}
			return result.getOrThrow().templateJson().orElseThrow(() ->
					new IllegalStateException("Successful get operation did not return a template")
			);
		});
	}

}
