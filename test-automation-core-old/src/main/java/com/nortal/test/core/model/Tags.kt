package com.nortal.test.core.model

/**
 * This enum enumerates tags that affect scenarios execution
 */
enum class Tags(override val name: String, val description: String) {
    SEQUENTIAL(
        "@Sequential", "Marks scenario that must be executed in a sequential manner. " +
                "It is usually required because scenario affects global state and would impact other running scenarios"
    ),
    SKIP("@Skip", "Marks a scenario as skipped. As a result the scenario will not execute");

}