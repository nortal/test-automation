package com.nortal.test.postman.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostmanCollectionFileNameUtils {
	private static final String COLLECTION_PREFIX = "postman-";
	private static final String JSON_SUFFIX = ".json";

	public static String createFileName(final String featureId) {
		return COLLECTION_PREFIX + featureId + JSON_SUFFIX;
	}

	public static String getFeatureId(final String fileName) {
		return fileName.substring(COLLECTION_PREFIX.length(), fileName.length() - JSON_SUFFIX.length());
	}
}
