package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * An object containing path to file certificate, on the file system
 */
@Data
public class CERT {
	@JsonProperty("src")
	private Object src;
}
