package com.nortal.test.core.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RandomValuesUtils {

	public static String randomBan() {
		return "" + (long) ((Math.random() + 5) * 100000000L);
	}

	public static String randomMsisdn() {
		return "" + (long) ((Math.random() + 5) * 1000000000L);
	}

}
