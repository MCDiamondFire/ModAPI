package com.mcdiamondfire.proto;

import com.google.protobuf.Message;

/**
 * Represents a deserialized ModAPI message.
 *
 * @param id      the unique identifier of the ModAPI message type
 * @param message the deserialized Protocol Message instance
 */
public record ModAPIMessage(String id, Message message) {

}
