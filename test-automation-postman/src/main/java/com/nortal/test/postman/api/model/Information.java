package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Detailed description of the info block
 */
@Builder
@Data
public class Information {
	@JsonProperty("_postman_id")
	private String postmanID;

	@JsonProperty("description")
	private Description description;

	@JsonProperty("name")
	private String name;

	@JsonProperty("schema")
	private String schema;

	@JsonProperty("version")
	private CollectionVersion version;
}
