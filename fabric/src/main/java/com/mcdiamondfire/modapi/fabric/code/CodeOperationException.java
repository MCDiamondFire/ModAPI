package com.mcdiamondfire.modapi.fabric.code;

/**
 * Indicates that DiamondFire rejected a requested code operation.
 */
public final class CodeOperationException extends RuntimeException {
	
	private final CodeOperationResult.Error error;
	
	/**
	 * Creates an exception for a server-provided rejection.
	 *
	 * @param error the rejection reason
	 */
	public CodeOperationException(CodeOperationResult.Error error) {
		super("Code operation failed: " + error);
		this.error = error;
	}
	
	/**
	 * Returns the server-provided rejection reason.
	 *
	 * @return the rejection reason
	 */
	public CodeOperationResult.Error getError() {
		return error;
	}
	
}
