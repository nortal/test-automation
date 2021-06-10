package com.nortal.test.postman.api.model;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * Items are entities which contain an actual HTTP request, and sample responses attached to it.
 * <p>
 * One of the primary goals of Postman is to organize the development of APIs. To this end, it is necessary to be able to group requests together.
 * This can be achived using 'Folders'. A folder just is an ordered set of requests.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Item {
	@JsonProperty("id")
	private String id;

	@JsonProperty("name")
	private String name;

	@JsonProperty("description")
	private String description;

	@Singular
	@JsonProperty("event")
	private List<Event> events;

	@JsonProperty("protocolProfileBehavior")
	private Map<String, Object> protocolProfileBehavior;

	@JsonProperty("request")
	private Request request;

	@Singular
	@JsonProperty("response")
	private List<Response> responses;

	@JsonProperty("variable")
	private List<Variable> variable;

	@JsonProperty("auth")
	private Auth auth;

	@JsonProperty("item")
	private List<Item> items;
}
