package com.nortal.test.postman.util;

import com.nortal.test.postman.constant.PostmanVariableConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostmanVariableUtils {

	public static String asVariableKey(final String variableKey) {
		return String.format(PostmanVariableConstants.VAR_PLACEHOLDER, variableKey);
	}
}
