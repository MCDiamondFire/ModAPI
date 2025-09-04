package com.mcdiamondfire.proto;

import com.google.protobuf.Message;
import org.jspecify.annotations.Nullable;

/**
 * Represents a deserialized ModAPI message.
 *
 * @param id        the unique identifier of the ModAPI message type
 * @param message   the deserialized Protocol Message instance
 * @param requestId the ID of the original request message, or null if not applicable
 */
public record ModAPIMessage(String id, Message message, @Nullable Integer requestId) {
	
}
