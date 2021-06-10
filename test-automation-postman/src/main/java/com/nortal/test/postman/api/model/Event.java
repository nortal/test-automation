package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Postman allows you to configure scripts to run when specific events occur. These scripts are stored here, and can be referenced in the collection
 * by their ID.
 * <p>
 * Defines a script associated with an associated event name
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {
	@JsonProperty("disabled")
	private Boolean disabled;

	@JsonProperty("id")
	private String id;

	@JsonProperty("listen")
	private String listen;

	@JsonProperty("script")
	private EventScript script;
}
