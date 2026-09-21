package com.mcdiamondfire.modapi.fabric.model.plot;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Information displayed on a code action icon.
 *
 * @param material               the action icon material as a namespaced identifier
 * @param name                   the displayed action name
 * @param signName               the action sign name
 * @param description            action description
 * @param additionalInfo         additional note sections
 * @param tagCount               the number of action tags, when applicable
 * @param arguments              arguments accepted by the action
 * @param returnValues           values returned by the action
 * @param cancellable            whether an event is cancellable, when applicable
 * @param cancelledAutomatically whether an event is cancelled automatically, when applicable
 */
public record ActionReference(
		Identifier material,
		Component name,
		String signName,
		List<Component> description,
		List<Note> additionalInfo,
		OptionalInt tagCount,
		List<Argument> arguments,
		List<ReturnValue> returnValues,
		Optional<Boolean> cancellable,
		Optional<Boolean> cancelledAutomatically
) {
	
	/**
	 * Creates an immutable action reference.
	 */
	public ActionReference {
		description = List.copyOf(description);
		additionalInfo = List.copyOf(additionalInfo);
		arguments = List.copyOf(arguments);
		returnValues = List.copyOf(returnValues);
	}
	
	/**
	 * A code value type.
	 */
	public enum ValueType {
		/// Any value.
		ANY_TYPE,
		/// A variable.
		VARIABLE,
		/// A number.
		NUMBER,
		/// A string.
		TEXT,
		/// Styled text.
		COMPONENT,
		/// A location.
		LOCATION,
		/// A vector.
		VECTOR,
		/// A list.
		LIST,
		/// A dictionary.
		DICT,
		/// A potion effect.
		POTION,
		/// A particle effect.
		PARTICLE,
		/// A sound.
		SOUND,
		/// A spawn egg.
		SPAWN_EGG,
		/// A projectile.
		PROJECTILE,
		/// A vehicle.
		VEHICLE,
		/// An entity type.
		ENTITY_TYPE,
		/// A block.
		BLOCK,
		/// A block tag.
		BLOCK_TAG,
		/// An item.
		ITEM,
		/// A byte.
		BYTE,
		/// No value.
		NONE,
		/// A value type introduced by a newer protocol version.
		UNKNOWN
	}
	
	/**
	 * A value returned by a code action.
	 */
	public sealed interface ReturnValue permits StandardReturnValue, SimpleReturnValue {
	
	}
	
	/**
	 * Component lines displayed as a note.
	 *
	 * @param lines displayed lines
	 */
	public record Note(List<Component> lines) {
		
		/**
		 * Creates an immutable note.
		 */
		public Note {
			lines = List.copyOf(lines);
		}
		
	}
	
	/**
	 * An argument accepted by a code action.
	 *
	 * @param type        the accepted value type
	 * @param plural      whether multiple values may be supplied
	 * @param optional    whether the argument is optional
	 * @param description lines describing the argument
	 * @param notes       additional notes about the argument
	 */
	public record Argument(
			ValueType type,
			boolean plural,
			boolean optional,
			List<Component> description,
			List<Note> notes
	) {
		
		/**
		 * Creates an immutable argument description.
		 */
		public Argument {
			description = List.copyOf(description);
			notes = List.copyOf(notes);
		}
		
	}
	
	/**
	 * A return value with component descriptions.
	 *
	 * @param type        the returned value type
	 * @param description lines describing the return value
	 */
	public record StandardReturnValue(ValueType type, List<Component> description) implements ReturnValue {
		
		/**
		 * Creates an immutable standard return value.
		 */
		public StandardReturnValue {
			description = List.copyOf(description);
		}
		
	}
	
	/**
	 * A return value represented only by displayed text.
	 *
	 * @param text the displayed text
	 */
	public record SimpleReturnValue(Component text) implements ReturnValue {
	
	}
	
}
