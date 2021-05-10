package com.nortal.test.postman.api.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A script is a snippet of Javascript code that can be used to to perform setup or teardown operations on a particular response.
 */
@lombok.Data
public class Script {
	@lombok.Getter(onMethod_ = {@JsonProperty("exec")})
	@lombok.Setter(onMethod_ = {@JsonProperty("exec")})
	private List<String> exec;
	@lombok.Getter(onMethod_ = {@JsonProperty("id")})
	@lombok.Setter(onMethod_ = {@JsonProperty("id")})
	private String id;
	@lombok.Getter(onMethod_ = {@JsonProperty("name")})
	@lombok.Setter(onMethod_ = {@JsonProperty("name")})
	private String name;
	@JsonProperty("src")
	private URL src;
	@lombok.Getter(onMethod_ = {@JsonProperty("type")})
	@lombok.Setter(onMethod_ = {@JsonProperty("type")})
	private String type;
}
