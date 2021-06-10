package com.nortal.test.postman.generate;

import java.util.Set;

import com.nortal.test.postman.PostmanScenarioRequestContextProvider;
import com.nortal.test.postman.constant.PostmanVariableConstants;
import com.nortal.test.postman.util.PostmanVariableUtils;
import org.springframework.stereotype.Component;
import com.google.common.collect.ImmutableSet;
import com.nortal.test.postman.api.model.Header;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class PostmanHeaderTransformer {
	private static final Set<String> OAUTH_HEADERS = ImmutableSet.of("authorization", "x-auth-originator");
	private static final String INTERACTION_ID_HEADER = "interactionid";

	private final PostmanScenarioRequestContextProvider postmanScenarioRequestContextProvider;

	public Header createHeader(final String key, final String value) {
		String headerValue = value;
		if (value != null) {

			if (OAUTH_HEADERS.contains(key.toLowerCase())) {
				final String jwt = postmanScenarioRequestContextProvider.get().getJwtToken();
				if (jwt != null) {
					headerValue = headerValue.replace(jwt, PostmanVariableUtils.asVariableKey(PostmanVariableConstants.VAR_JWT));
				}
			} else if (INTERACTION_ID_HEADER.equalsIgnoreCase(key)) {
				headerValue = PostmanVariableUtils.asVariableKey(PostmanVariableConstants.VAR_INTERACTION_ID);
			}
		}

		return Header.builder()
				.key(key)
				.value(headerValue)
				.build();
	}
}
