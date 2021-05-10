package com.nortal.test.core.configuration;

import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This property class holds properties related to how we run scenarios.
 */
@Component
@Data
@ConfigurationProperties(prefix = "scenario")
public class ScenarioProperties {

	/**
	 * Determines what the scope of the test run is. Depending on the scope different scenarios are executed.
	 * MOCKED - default scope where "unscoped", @Scope:mocked and @Scope:hybrid scenarios are run
	 * E2E - non mocked scope where @Scope:hybrid and @Scope:e2e scenarios are run
	 */
	private Scope scope;

	/**
	 * Determines which scenarios to run from domain perspective.
	 * DEFAULT - regular scenarios used by the dev teams for signoff
	 * RELEASE - scenarios used by release SDETS
	 */
	private Domain domain;

	@Getter
	@RequiredArgsConstructor
	public enum Scope {
		MOCKED(Set.of("@SCOPE:MOCKED", "@SCOPE:HYBRID")),
		E2E(Set.of("@SCOPE:HYBRID", "@SCOPE:E2E"));

		private final Set<String> tags;
	}

	@Getter
	@RequiredArgsConstructor
	public enum Domain {
		DEFAULT("classpath:behavior"),
		RELEASE("classpath:release-behavior");
		private final String featurePath;
	}

}
