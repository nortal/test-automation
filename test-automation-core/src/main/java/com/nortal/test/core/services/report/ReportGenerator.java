package com.nortal.test.core.services.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.nortal.test.core.configuration.ReportProperties;
import com.nortal.test.core.services.report.html.TmoReportBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.json.support.Status;

/**
 * This class is responsible for generating an html report after the test suite is done running.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ReportGenerator {
	private static final String JSON_SUFFIX = ".json";

	private final ReportProperties reportProperties;
	private final ReportUploader uploader;
	private final ReportMailSender mailer;
	private final FileHistoryService historyService;
	private final ScenarioSkipService skipService;
//	private final PostmanAutomationCollectionGenerator postmanAutomationCollectionGenerator;

	@Value("${integration.hosts.report_upload:#{null}}")
	private String uploadHost;

	public void generate() {
		final File reportOutputDirectory = new File(reportProperties.getOutput());
		final Configuration configuration = new Configuration(reportOutputDirectory, reportProperties.getBuildName());
		configuration.setNotFailingStatuses(Collections.singleton(Status.SKIPPED));
		configuration.setBuildNumber(reportProperties.getBuildNumber());

		new TmoReportBuilder(getAllJsons(), configuration, getBaseUrl(),
		                     reportProperties.getCapabilities(),
		                     skipService.getSkippedScenarios(),
		                     historyService.getCommits()).generateReports();

		uploader.upload();
		mailer.mail();
	}

	private String getBaseUrl() {
		if (reportProperties.shouldReport()) {
			return String.format("%s/v2/testsupport/automation/reports/%s/%s", uploadHost, reportProperties.getBuildName(),
			                     reportProperties.getBuildNumber());
		}

		//if there is no uploaded report we short circuit all the links
		return "data:text/plain;base64,T29wcyEgTG9va3MgbGlrZSB0aGlzIHJlcG9ydCB3YXMgZ2VuZXJhdGVkIGZyb20gYSBsb2NhbCBydW4gYW5kIHdhcyBub3QgdXBsb2FkZWQgdG8gdGhlIHNlcnZlciBmb3Igc3RvcmFnZS4gVGhpcyBtZWFucyB0aGF0IGFsbCBleHRlcm5hbCBmYWNpbmcgbGlua3Mgd2lsbCBiZSBzaG9ydC1jaXJjdWl0ZWQu";
	}

	private List<String> getAllJsons() {
		try {
			return Files.list(Paths.get(reportProperties.getOutput()))
					.map(Path::toString)
					.filter(path -> path.endsWith(JSON_SUFFIX))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new AssertionError("Failed to generate test report due to exception.", e);
		}
	}

}
