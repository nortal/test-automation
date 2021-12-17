package com.nortal.test.core.model

import java.util.*

/**
 * This enum specifies the environments that are available for us to run tests against.
 * It should be expanded when new environments appear.
 */
enum class TargetEnvironment {
    LOCAL, SBX, DEV, DEV2, QAT, REL, STG;

    companion object {
        /**
         * Check if the profile matches any of the environments available to us.
         * @param profile to check
         * @return true if matches
         */
        fun isAssignable(profile: String): Boolean {
            return Arrays.stream(values()).map { obj: TargetEnvironment -> obj.toString() }
                .anyMatch { anotherString: String? -> profile.equals(anotherString, ignoreCase = true) }
        }
    }
}