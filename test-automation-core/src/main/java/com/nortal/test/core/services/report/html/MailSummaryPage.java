package com.nortal.test.core.services.report.html;

import java.util.List;
import java.util.stream.Collectors;

import com.nortal.test.core.services.report.cucumber.SkippedScenarios;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportResult;
import net.masterthought.cucumber.generators.AbstractPage;
import net.masterthought.cucumber.json.support.TagObject;

public class MailSummaryPage extends AbstractPage {

	public static final String WEB_PAGE = "mail-summary.html";

	private final String reportBaseUrl;
	private final List<String> capabilities;
	private final List<ReportErrorMessage> errors;
	private final SkippedScenarios skippedScenarios;
	private final ChangedFilesReport gdChanges;

	public MailSummaryPage(ReportResult reportResult, Configuration configuration, final String reportBaseUrl,
			final List<String> capabilities, final List<ReportErrorMessage> errors,
			final SkippedScenarios skippedScenarios,
			final ChangedFilesReport gdChanges) {
		super(reportResult, "mailSummary.vm", configuration);
		this.reportBaseUrl = reportBaseUrl;
		this.capabilities = capabilities;
		this.errors = errors;
		this.skippedScenarios = skippedScenarios;
		this.gdChanges = gdChanges;
	}

	@Override
	public String getWebPage() {
		return WEB_PAGE;
	}

	@Override
	public void prepareReport() {
		context.put("all_features", reportResult.getAllFeatures());
		context.put("report_summary", reportResult.getFeatureReport());
		context.put("classifications", configuration.getClassifications());
		context.put("absolutePrefix", reportBaseUrl);
		context.put("capabilityTags", getCapabilitiesTags());
		context.put("error_messages", errors);
		context.put("skippedScenarios", skippedScenarios);
		context.put("gdChanges", gdChanges);
	}

	private List<TagObject> getCapabilitiesTags() {
		return reportResult.getAllTags().stream()
						   .filter(tag -> capabilities.contains(tag.getName()))
						   .collect(Collectors.toList());
	}

}
