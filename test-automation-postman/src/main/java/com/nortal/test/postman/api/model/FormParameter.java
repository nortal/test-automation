package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FormParameter {
	@JsonProperty("contentType")
	private String contentType;

	@JsonProperty("description")
	private Description description;

	@JsonProperty("disabled")
	private Boolean disabled;

	@JsonProperty("key")
	private String key;

	@JsonProperty("type")
	private FormParameterType type;

	@JsonProperty("value")
	private String value;

	@JsonProperty("src")
	private Src src;
}
