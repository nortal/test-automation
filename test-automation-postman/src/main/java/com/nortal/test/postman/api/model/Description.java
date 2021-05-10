package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Description {
	@JsonProperty("content")
	private String content;

	@JsonProperty("type")
	private String type;

	@JsonProperty("version")
	private Object version;
}
