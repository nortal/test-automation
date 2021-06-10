package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents an attribute for any authorization method provided by Postman. For example `username` and `password` are set as auth attributes for
 * Basic Authentication method.
 */
@Data
public class ApikeyElement {
	@JsonProperty("key")
	private String key;

	@JsonProperty("type")
	private String type;

	@JsonProperty("value")
	private Object value;
}
