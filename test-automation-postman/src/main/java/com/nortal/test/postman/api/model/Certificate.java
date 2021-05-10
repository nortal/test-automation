package com.nortal.test.postman.api.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A representation of an ssl certificate
 */
@Data
public class Certificate {
	@JsonProperty("cert")
	private CERT cert;

	@JsonProperty("key")
	private Key key;

	@JsonProperty("matches")
	private List<Object> matches;

	@JsonProperty("name")
	private String name;

	@JsonProperty("passphrase")
	private String passphrase;
}
