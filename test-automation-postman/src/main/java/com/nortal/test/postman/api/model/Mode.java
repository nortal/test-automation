package com.nortal.test.postman.api.model;

import java.io.IOException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Postman stores the type of data associated with this request in this field.
 */
public enum Mode {
	FILE,
	FORMDATA,
	GRAPHQL,
	RAW,
	URLENCODED;

	@JsonCreator
	public static Mode forValue(String value) throws IOException {
        if (value.equals("file")) {
            return FILE;
        }
        if (value.equals("formdata")) {
            return FORMDATA;
        }
        if (value.equals("graphql")) {
            return GRAPHQL;
        }
        if (value.equals("raw")) {
            return RAW;
        }
        if (value.equals("urlencoded")) {
            return URLENCODED;
        }
		throw new IOException("Cannot deserialize Mode");
	}

	@JsonValue
	public String toValue() {
		switch (this) {
		case FILE:
			return "file";
		case FORMDATA:
			return "formdata";
		case GRAPHQL:
			return "graphql";
		case RAW:
			return "raw";
		case URLENCODED:
			return "urlencoded";
		}
		return null;
	}
}
