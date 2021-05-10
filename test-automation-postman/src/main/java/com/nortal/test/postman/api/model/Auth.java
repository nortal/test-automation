package com.nortal.test.postman.api.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents authentication helpers provided by Postman
 */
@Data
public class Auth {
	@JsonProperty("apikey")
	private List<ApikeyElement> apikey;

	@JsonProperty("awsv4")
	private List<ApikeyElement> awsv4;

	@JsonProperty("basic")
	private List<ApikeyElement> basic;

	@JsonProperty("bearer")
	private List<ApikeyElement> bearer;

	@JsonProperty("digest")
	private List<ApikeyElement> digest;

	@JsonProperty("hawk")
	private List<ApikeyElement> hawk;

	@JsonProperty("noauth")
	private Object noauth;

	@JsonProperty("ntlm")
	private List<ApikeyElement> ntlm;

	@JsonProperty("oauth1")
	private List<ApikeyElement> oauth1;

	@JsonProperty("oauth2")
	private List<ApikeyElement> oauth2;

	@JsonProperty("type")
	private AuthType type;
}
