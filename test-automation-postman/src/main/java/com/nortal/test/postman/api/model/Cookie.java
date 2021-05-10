package com.nortal.test.postman.api.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A Cookie, that follows the [Google Chrome format](https://developer.chrome.com/extensions/cookies)
 */
@Data
public class Cookie {
	@JsonProperty("domain")
	private String domain;

	@JsonProperty("expires")
	private ResponseTime expires;

	@JsonProperty("extensions")
	private List<Object> extensions;

	@JsonProperty("hostOnly")
	private Boolean hostOnly;

	@JsonProperty("httpOnly")
	private Boolean httpOnly;

	@JsonProperty("maxAge")
	private String maxAge;

	@JsonProperty("name")
	private String name;

	@JsonProperty("path")
	private String path;

	@JsonProperty("secure")
	private Boolean secure;

	@JsonProperty("session")
	private Boolean session;

	@JsonProperty("value")
	private String value;
}
