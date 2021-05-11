package com.nortal.test.core.configuration;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Setter;

@Component
@Setter
@ConfigurationProperties(prefix = "report")
public class ReportProperties {
	private boolean enabled;
	private String buildName;
	private String buildNumber;
	private String output;
	private String capabilities;
	private Integer gdHistoryDays = 14;
	private boolean includeGdHistory = false;

	/**
	 * This method controls whether the report should be propagated.
	 * If the tests are running in CI env, then the report should be uploaded to S3 and mail should be sent.
	 * Otherwise it should not.
	 *
	 * @return true if report should be sent
	 */
	public boolean shouldReport() {
		return enabled && buildName != null && buildNumber != null;
	}

	public String getBuildName() {
		return buildName;
	}

	public String getBuildNumber() {
		return buildNumber;
	}

	/**
	 * Returns output directory for the report files.
	 * @return relative dir path
	 */
	public String getOutput() {
		return output == null ? "build/test-output" : output;
	}

	/**
	 * Returns a list of cucumber tags that should be attached as Capabilities to the report.
	 * @return list of tag names.
	 */
	public List<String> getCapabilities() {
		if (capabilities == null) {
			throw new IllegalStateException("report.capabilities property should be set!");
		}
		return Arrays.asList(capabilities.split(","));
	}

	/**
	 * Whether report uploading is enabled.
	 * @return true if enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * How many days of GD history to include in the report
	 * @return
	 */
	public Integer getGdHistoryDays() {
		return gdHistoryDays;
	}

	/**
	 * Certain jobs don't care about GD (e.g. E2E) thus it is configurable whether GD history should be included.
	 * @return true if GD history should be included
	 */
	public boolean shouldIncludeGdHistory() {
		return includeGdHistory;
	}
}
