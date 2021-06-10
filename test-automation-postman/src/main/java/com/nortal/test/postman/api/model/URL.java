package com.nortal.test.postman.api.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class URL {
	@JsonProperty("hash")
	private String hash;

	@JsonProperty("host")
	private String host;

	@JsonProperty("path")
	private List<String> path;

	@JsonProperty("port")
	private String port;

	@JsonProperty("protocol")
	private String protocol;

	@Singular
	@JsonProperty("query")
	private List<QueryParam> queries;

	@JsonProperty("raw")
	private String raw;

	@JsonProperty("variable")
	private List<Variable> variables;
}
