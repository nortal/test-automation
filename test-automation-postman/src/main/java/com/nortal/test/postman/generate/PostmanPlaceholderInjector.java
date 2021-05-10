package com.nortal.test.postman.generate;

import java.util.Map;
import java.util.stream.Collectors;

import com.nortal.test.postman.PostmanHostAware;
import com.nortal.test.postman.PostmanScenarioRequestContext;
import com.nortal.test.postman.util.PostmanVariableUtils;
import org.springframework.stereotype.Component;
import com.nortal.test.postman.api.model.URL;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostmanPlaceholderInjector {
	private final PostmanHostAware postmanHostAware;

	public void injectPlaceholdersAndScriptIfNecessary(final PostmanScenarioRequestContext requestContext, final URL url) {
		injectHostPlaceholders(url);
		injectUrlPlaceholders(requestContext, url);
	}

	private void injectUrlPlaceholders(final PostmanScenarioRequestContext requestContext, final URL url) {
		if (url.getPath() != null) {
			url.setPath(url.getPath().stream()
					            .map(pathPart -> alterPathIfNecessary(requestContext, pathPart))
					            .collect(Collectors.toList()));
		}
	}

	private String alterPathIfNecessary(final PostmanScenarioRequestContext requestContext, final String pathPart) {
		//TODO this should be configurable
		/*if (pathPart.equals(requestContext.getCartId())) {
			return asVariableKey(PostmanVariableConstants.VAR_CART_ID);
		} else if (pathPart.equals(requestContext.getProfileId())) {
			return asVariableKey(PostmanVariableConstants.VAR_PROFILE_ID);
		} else if (requestContext.getLineIds().contains(pathPart)) {
			return asVariableKey(PostmanVariableConstants.VAR_LINE_ID); //TODO might need separate vars for multi line
		}*/

		return pathPart;
	}

	private void injectHostPlaceholders(final URL url) {
		final Map.Entry<String, String> hostEntry = postmanHostAware.getHosts().entrySet().stream()
				.filter(stringStringEntry -> stringStringEntry.getValue().equalsIgnoreCase(url.getHost()))
				.findFirst().orElseThrow();

		final String hostPlaceholder = PostmanVariableUtils.asVariableKey(hostEntry.getKey());

		url.setHost(hostPlaceholder);
		url.setRaw(url.getRaw().replace(hostEntry.getValue(), hostPlaceholder));
	}

}
