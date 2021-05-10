package com.nortal.test.core.model;

/**
 * This enum enumerates tags that affect scenarios execution
 */
public enum Tags {
	SEQUENTIAL("@Sequential", "Marks scenario that must be executed in a sequential manner. " +
			"It is usually required because scenario affects global state and would impact other running scenarios"),
	SKIP("@Skip", "Marks a scenario as skipped. As a result the scenario will not execute");

	private final String name;
	private final String description;

	Tags(final String name, final String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
