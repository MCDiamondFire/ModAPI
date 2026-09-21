package com.mcdiamondfire.modapi.fabric.code;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Objects;

/**
 * A code template represented as encoded template data.
 *
 * @param value the template contents
 */
public record CodeTemplate(String value) {
	
	/**
	 * Constructs a new code template.
	 * It is recommended to use the static factory methods.
	 *
	 * @param value the template contents
	 */
	public CodeTemplate {
		Objects.requireNonNull(value, "value");
	}
	
	/**
	 * Creates a template from template JSON.
	 *
	 * @param json template JSON
	 * @return the template value
	 */
	public static CodeTemplate json(String json) {
		JsonObject object = JsonParser.parseString(json).getAsJsonObject();
		String code = object.get("code").getAsString();
		
		return new CodeTemplate(code);
	}
	
	/**
	 * Creates a template from encoded template data.
	 *
	 * @param data encoded template data
	 * @return the template value
	 */
	public static CodeTemplate data(String data) {
		return new CodeTemplate(data);
	}
	
}
