package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Using the Proxy, you can configure your custom proxy into the postman for particular url match
 */
@Data
public class ProxyConfig {
	@JsonProperty("disabled")
	private Boolean disabled;

	@JsonProperty("host")
	private String host;

	@JsonProperty("match")
	private String match;

	@JsonProperty("port")
	private Long port;

	@JsonProperty("tunnel")
	private Boolean tunnel;
}
