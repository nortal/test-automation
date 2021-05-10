package com.nortal.test.postman.api.model;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This field contains the data usually contained in the request body.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Body {
	@JsonProperty("disabled")
	private Boolean disabled;

	@JsonProperty("file")
	private File file;

	@JsonProperty("formdata")
	private List<FormParameter> formdata;

	@JsonProperty("graphql")
	private Map<String, Object> graphql;

	@JsonProperty("mode")
	private Mode mode;

	@JsonProperty("raw")
	private String raw;

	@JsonProperty("urlencoded")
	private List<URLEncodedParameter> urlencoded;
}
