package com.nortal.test.postman.api.model;

import java.io.IOException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A variable may have multiple types. This field specifies the type of the variable.
 */
public enum VariableType {
	ANY,
	BOOLEAN,
	NUMBER,
	STRING;

	@JsonCreator
	public static VariableType forValue(String value) throws IOException {
		if (value.equals("any")) {
			return ANY;
		}
		if (value.equals("boolean")) {
			return BOOLEAN;
		}
		if (value.equals("number")) {
			return NUMBER;
		}
		if (value.equals("string")) {
			return STRING;
		}
		throw new IOException("Cannot deserialize VariableType");
	}

	@JsonValue
	public String toValue() {
		switch (this) {
		case ANY:
			return "any";
		case BOOLEAN:
			return "boolean";
		case NUMBER:
			return "number";
		case STRING:
			return "string";
		}
		return null;
	}
}
