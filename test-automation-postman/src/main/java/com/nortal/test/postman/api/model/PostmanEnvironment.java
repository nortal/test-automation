package com.nortal.test.postman.api.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostmanEnvironment {

	private String id;
	private String name;

	@Singular
	private List<PostmanEnvironmentValue> values;

	@JsonProperty("_postman_variable_scope")
	private String variableScope;
	@JsonProperty("_postman_exported_at")
	private String exportedAt;
	@JsonProperty("_postman_exported_using")
	private String exportedUsing;
}
