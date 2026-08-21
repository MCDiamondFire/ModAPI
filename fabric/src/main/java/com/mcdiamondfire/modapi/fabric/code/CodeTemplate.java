package com.mcdiamondfire.modapi.fabric.code;

import java.util.Objects;

/**
 * A code template represented as either template JSON or encoded template data.
 *
 * @param format the representation stored in {@code value}
 * @param value  the template contents
 */
public record CodeTemplate(Format format, String value) {
	
	/**
	 * Constructs a new code template.
	 * It is recommended to use the static factory methods.
	 *
	 * @param format the template format
	 * @param value  the template contents
	 */
	public CodeTemplate {
		Objects.requireNonNull(format, "format");
		Objects.requireNonNull(value, "value");
	}
	
	/**
	 * Creates a template from template JSON.
	 *
	 * @param json template JSON
	 * @return the template value
	 */
	public static CodeTemplate json(String json) {
		return new CodeTemplate(Format.JSON, json);
	}
	
	/**
	 * Creates a template from encoded template data.
	 *
	 * @param data encoded template data
	 * @return the template value
	 */
	public static CodeTemplate data(String data) {
		return new CodeTemplate(Format.DATA, data);
	}
	
	/**
	 * A supported template representation.
	 */
	public enum Format {
		/// Template JSON.
		JSON,
		/// Encoded template data.
		DATA
	}
	
}
