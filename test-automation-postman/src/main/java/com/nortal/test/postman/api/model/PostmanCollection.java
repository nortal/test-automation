package com.nortal.test.postman.api.model;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Builder
@Data
public class PostmanCollection {
	@JsonProperty("auth")
	private Auth auth;

	@JsonProperty("event")
	private List<Event> event;

	@JsonProperty("info")
	private Information info;

	@Singular
	@JsonProperty("item")
	private List<Item> items;

	@JsonProperty("protocolProfileBehavior")
	private Map<String, Object> protocolProfileBehavior;

	@JsonProperty("variable")
	private List<Variable> variable;
}
