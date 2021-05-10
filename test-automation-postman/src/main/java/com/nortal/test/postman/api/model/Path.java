package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Path {
	@JsonProperty("type")
	private String type;

	@JsonProperty("value")
	private String value;
}
