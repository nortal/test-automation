package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * An object containing path to file containing private key, on the file system
 */
@Data
public class Key {
	@JsonProperty("src")
	private Object src;
}
