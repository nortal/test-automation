package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class URLEncodedParameter {
	@JsonProperty("description")
	private Description description;
	@JsonProperty("disabled")
	private Boolean disabled;
	@JsonProperty("key")
	private String key;
	@JsonProperty("value")
	private String value;
}
