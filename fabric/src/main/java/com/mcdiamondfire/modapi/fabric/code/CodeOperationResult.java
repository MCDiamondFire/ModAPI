package com.mcdiamondfire.modapi.fabric.code;

import java.util.Optional;

/**
 * The outcome of one code operation.
 */
public sealed interface CodeOperationResult permits CodeOperationResult.Success, CodeOperationResult.Failure {
	
	/**
	 * Returns this result as a success, throwing an exception if it is a failure.
	 *
	 * @return a success result
	 */
	Success getOrThrow();
	
	/**
	 * Reasons a code operation may be rejected.
	 */
	enum Error {
		/// The player lacks developer ranks on the plot.
		NO_PERMISSION,
		/// An operation requiring a location did not receive one.
		NO_LOCATION,
		/// The location is outside the code region or is not a valid code block position.
		INVALID_LOCATION,
		/// An operation requiring a line starter name did not receive one.
		NO_ACTION,
		/// The requested line starter does not exist.
		ACTION_NOT_FOUND,
		/// Required input for a placement or replacement operation was missing.
		NO_LOCATION_OR_TEMPLATE,
		/// A placement or replacement location or template is invalid.
		INVALID_LOCATION_OR_TEMPLATE,
		/// The template does not fit at the requested location.
		TEMPLATE_NOT_FIT,
		/// The supplied template is not a valid code template.
		INVALID_TEMPLATE,
		/// The server failed while applying the operation.
		INTERNAL_ERROR,
		/// No operation was supplied.
		NO_OPERATION,
		/// A rejection reason introduced by a newer protocol version.
		UNKNOWN
	}
	
	/**
	 * A successful operation, optionally containing template JSON returned by a get operation.
	 *
	 * @param templateJson template JSON for a get operation, otherwise empty
	 */
	record Success(Optional<String> templateJson) implements CodeOperationResult {
		
		@Override
		public Success getOrThrow() {
			return this;
		}
		
	}
	
	/**
	 * An operation rejected by the server.
	 *
	 * @param error the rejection reason
	 */
	record Failure(Error error) implements CodeOperationResult {
		
		@Override
		public Success getOrThrow() {
			throw new IllegalStateException("Operation did not succeed: " + error);
		}
		
	}
	
}
