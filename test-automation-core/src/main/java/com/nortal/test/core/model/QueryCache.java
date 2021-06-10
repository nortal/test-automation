package com.nortal.test.core.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class QueryCache {
	private final Map<String, String> queryToIdentifier = new HashMap<>();

	public void add(String query, String resolvedIdentifier) {
		queryToIdentifier.put(query, resolvedIdentifier);
	}

	public Optional<String> get(String query) {
		return Optional.ofNullable(queryToIdentifier.get(query));
	}
}
