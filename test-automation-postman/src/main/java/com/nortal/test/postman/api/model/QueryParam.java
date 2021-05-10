package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryParam {
	@JsonProperty("description")
	private Description description;

	@JsonProperty("disabled")
	private Boolean disabled;

	@JsonProperty("key")
	private String key;

	@JsonProperty("value")
	private String value;
}
