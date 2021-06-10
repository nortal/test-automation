package com.nortal.test.postman.api.model;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Response {
	@JsonProperty("id")
	private String id;

	@JsonProperty("name")
	private String name;

	@JsonProperty("body")
	private String body;

	@JsonProperty("code")
	private Long code;

	@JsonProperty("cookie")
	private List<Cookie> cookie;

	@Singular
	@JsonProperty("header")
	private List<Header> headers;

	@JsonProperty("originalRequest")
	private Request originalRequest;

	@JsonProperty("responseTime")
	private ResponseTime responseTime;

	@JsonProperty("status")
	private String status;

	@JsonProperty("timings")
	private Map<String, Object> timings;
}
