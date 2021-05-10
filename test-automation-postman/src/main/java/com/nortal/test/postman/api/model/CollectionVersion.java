package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CollectionVersion {
	@JsonProperty("identifier")
	private String identifier;

	@JsonProperty("major")
	private long major;

	@JsonProperty("meta")
	private Object meta;

	@JsonProperty("minor")
	private long minor;

	@JsonProperty("patch")
	private long patch;
}
