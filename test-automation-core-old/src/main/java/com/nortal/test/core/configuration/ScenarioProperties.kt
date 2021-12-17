package com.nortal.test.core.configuration

import lombok.Data
import lombok.Getter
import lombok.RequiredArgsConstructor
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * This property class holds properties related to how we run scenarios.
 */
@Component
@Data
@ConfigurationProperties(prefix = "test-automation.scenario")
class ScenarioProperties {
    /**
     * Determines what the scope of the test run is. Depending on the scope different scenarios are executed.
     * MOCKED - default scope where "unscoped", @Scope:mocked and @Scope:hybrid scenarios are run
     * E2E - non mocked scope where @Scope:hybrid and @Scope:e2e scenarios are run
     */
    private val scope: Scope? = null

    /**
     * Determines which scenarios to run from domain perspective.
     * DEFAULT - regular scenarios used by the dev teams for signoff
     * RELEASE - scenarios used by release SDETS
     */
    private val domain: Domain? = null

    @Getter
    @RequiredArgsConstructor
    enum class Scope {
        MOCKED(java.util.Set.of("@SCOPE:MOCKED", "@SCOPE:HYBRID")), E2E(java.util.Set.of("@SCOPE:HYBRID", "@SCOPE:E2E"));

        private val tags: Set<String>? = null
    }

    @Getter
    @RequiredArgsConstructor
    enum class Domain {
        DEFAULT("classpath:behavior"), RELEASE("classpath:release-behavior");

        private val featurePath: String? = null
    }
}