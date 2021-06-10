package com.nortal.test.core.model;

import java.util.Arrays;

/**
 * This enum specifies the environments that are available for us to run tests against.
 * It should be expanded when new environments appear.
 */
public enum TargetEnvironment {
	LOCAL,
	SBX,
	DEV,
	DEV2,
	QAT,
	REL,
	STG;

	/**
	 * Check if the profile matches any of the environments available to us.
	 * @param profile to check
	 * @return true if matches
	 */
	public static boolean isAssignable(String profile) {
		return Arrays.stream(TargetEnvironment.values()).map(TargetEnvironment::toString).anyMatch(profile::equalsIgnoreCase);
	}
}
