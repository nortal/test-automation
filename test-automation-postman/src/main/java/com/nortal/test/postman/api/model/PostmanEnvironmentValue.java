package com.nortal.test.postman.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostmanEnvironmentValue {
	private String key;
	private String value;
	private boolean enabled;
}
