package com.nortal.test.core.configuration

import lombok.Setter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.lang.IllegalStateException
import java.util.*

@Component
@Setter
@ConfigurationProperties(prefix = "test-automation.report")
class ReportProperties {
    /**
     * Whether report uploading is enabled.
     * @return true if enabled
     */
    val isEnabled = false
    val buildName: String? = null
    val buildNumber: String? = null

    /**
     * Returns output directory for the report files.
     * @return relative dir path
     */
    val output: String? = null
        get() = field ?: "build/test-output"
    private val capabilities: String? = null

    /**
     * How many days of GD history to include in the report
     * @return
     */
    val gdHistoryDays = 14
    private val includeGdHistory = false

    /**
     * This method controls whether the report should be propagated.
     * If the tests are running in CI env, then the report should be uploaded to S3 and mail should be sent.
     * Otherwise it should not.
     *
     * @return true if report should be sent
     */
    fun shouldReport(): Boolean {
        return isEnabled && buildName != null && buildNumber != null
    }

    /**
     * Returns a list of cucumber tags that should be attached as Capabilities to the report.
     * @return list of tag names.
     */
    fun getCapabilities(): List<String> {
        checkNotNull(capabilities) { "report.capabilities property should be set!" }
        return Arrays.asList(*capabilities.split(",").toTypedArray())
    }

    /**
     * Certain jobs don't care about GD (e.g. E2E) thus it is configurable whether GD history should be included.
     * @return true if GD history should be included
     */
    fun shouldIncludeGdHistory(): Boolean {
        return includeGdHistory
    }
}