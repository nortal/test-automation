package com.nortal.test.postman.api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A script is a snippet of Javascript code that can be used to to perform setup or teardown operations on a particular response.
 */
@Data
public class Script {
	private List<String> exec;
	private String id;
	private String name;
	private URL src;
	private String type;
}
