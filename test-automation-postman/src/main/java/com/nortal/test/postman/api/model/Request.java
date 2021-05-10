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
public class Request {
	@JsonProperty("auth")
	private Auth auth;

	@JsonProperty("body")
	private Body body;

	@JsonProperty("certificate")
	private Certificate certificate;

	@JsonProperty("description")
	private Description description;

	@JsonProperty("header")
	@Singular
	private List<Header> headers;

	@JsonProperty("method")
	private String method;

	@JsonProperty("proxy")
	private ProxyConfig proxy;

	@JsonProperty("url")
	private URL url;
}
