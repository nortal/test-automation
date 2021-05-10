package com.nortal.test.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Utils for pretty printing objects as json
 */
public final class PrettyPrintUtils {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private PrettyPrintUtils() {
		//It's a static utils class
	}

	public static String prettyPrint(Object object) {
		try {
			return "\n" + GSON.toJson(object, Object.class);
		} catch (JsonSyntaxException e) {
			return object.toString();
		}
	}


}
