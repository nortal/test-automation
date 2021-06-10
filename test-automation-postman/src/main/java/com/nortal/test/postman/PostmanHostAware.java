package com.nortal.test.postman;

import java.util.Map;

public interface PostmanHostAware {
	/**
	 * Get hosts which are used to resolve hosts.
	 */
	Map<String, String> getHosts();
}
