package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class File {
	@JsonProperty("content")
	private String content;

	@JsonProperty("src")
	private String src;
}
