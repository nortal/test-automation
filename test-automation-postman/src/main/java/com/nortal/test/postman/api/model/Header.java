package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A representation for a list of headers
 * <p>
 * Represents a single HTTP Header
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Header {
	@JsonProperty("description")
	private Description description;

	@JsonProperty("disabled")
	private Boolean disabled;

	@JsonProperty("key")
	private String key;

	@JsonProperty("value")
	private String value;
}
