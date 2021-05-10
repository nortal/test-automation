package com.nortal.test.postman.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Collection variables allow you to define a set of variables, that are a *part of the collection*, as opposed to environments, which are separate
 * entities. *Note: Collection variables must not contain any sensitive information.*
 * <p>
 * Using variables in your Postman requests eliminates the need to duplicate requests, which can save a lot of time. Variables can be defined, and
 * referenced to from any part of a request.
 */
@Data
public class Variable {
	@lombok.Getter(onMethod_ = {@JsonProperty("description")})
	@lombok.Setter(onMethod_ = {@JsonProperty("description")})
	private Description description;
	@lombok.Getter(onMethod_ = {@JsonProperty("disabled")})
	@lombok.Setter(onMethod_ = {@JsonProperty("disabled")})
	private Boolean disabled;
	@lombok.Getter(onMethod_ = {@JsonProperty("id")})
	@lombok.Setter(onMethod_ = {@JsonProperty("id")})
	private String id;
	@lombok.Getter(onMethod_ = {@JsonProperty("key")})
	@lombok.Setter(onMethod_ = {@JsonProperty("key")})
	private String key;
	@lombok.Getter(onMethod_ = {@JsonProperty("name")})
	@lombok.Setter(onMethod_ = {@JsonProperty("name")})
	private String name;
	@lombok.Getter(onMethod_ = {@JsonProperty("system")})
	@lombok.Setter(onMethod_ = {@JsonProperty("system")})
	private Boolean system;
	@lombok.Getter(onMethod_ = {@JsonProperty("type")})
	@lombok.Setter(onMethod_ = {@JsonProperty("type")})
	private VariableType type;
	@lombok.Getter(onMethod_ = {@JsonProperty("value")})
	@lombok.Setter(onMethod_ = {@JsonProperty("value")})
	private Object value;
}
