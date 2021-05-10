package com.nortal.test.postman.api.model;

import java.io.IOException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AuthType {
	APIKEY,
	AWSV4,
	BASIC,
	BEARER,
	DIGEST,
	HAWK,
	NOAUTH,
	NTLM,
	OAUTH1,
	OAUTH2;

	@JsonCreator
	public static AuthType forValue(String value) throws IOException {
        if (value.equals("apikey")) {
            return APIKEY;
        }
        if (value.equals("awsv4")) {
            return AWSV4;
        }
        if (value.equals("basic")) {
            return BASIC;
        }
        if (value.equals("bearer")) {
            return BEARER;
        }
        if (value.equals("digest")) {
            return DIGEST;
        }
        if (value.equals("hawk")) {
            return HAWK;
        }
        if (value.equals("noauth")) {
            return NOAUTH;
        }
        if (value.equals("ntlm")) {
            return NTLM;
        }
        if (value.equals("oauth1")) {
            return OAUTH1;
        }
        if (value.equals("oauth2")) {
            return OAUTH2;
        }
		throw new IOException("Cannot deserialize AuthType");
	}

	@JsonValue
	public String toValue() {
		switch (this) {
		case APIKEY:
			return "apikey";
		case AWSV4:
			return "awsv4";
		case BASIC:
			return "basic";
		case BEARER:
			return "bearer";
		case DIGEST:
			return "digest";
		case HAWK:
			return "hawk";
		case NOAUTH:
			return "noauth";
		case NTLM:
			return "ntlm";
		case OAUTH1:
			return "oauth1";
		case OAUTH2:
			return "oauth2";
		}
		return null;
	}
}
