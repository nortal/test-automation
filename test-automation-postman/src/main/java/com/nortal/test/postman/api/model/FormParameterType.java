package com.nortal.test.postman.api.model;

import java.io.IOException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FormParameterType {
	FILE,
	TEXT;

	@JsonCreator
	public static FormParameterType forValue(String value) throws IOException {
        if (value.equals("file")) {
            return FILE;
        }
        if (value.equals("text")) {
            return TEXT;
        }
		throw new IOException("Cannot deserialize FormParameterType");
	}

	@JsonValue
	public String toValue() {
		switch (this) {
		case FILE:
			return "file";
		case TEXT:
			return "text";
		}
		return null;
	}
}
