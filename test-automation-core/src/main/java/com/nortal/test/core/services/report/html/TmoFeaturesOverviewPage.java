package com.nortal.test.core.services.report.html;

import java.util.List;

import com.nortal.test.core.services.report.cucumber.SkippedScenarios;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportResult;
import net.masterthought.cucumber.generators.FeaturesOverviewPage;

public class TmoFeaturesOverviewPage extends FeaturesOverviewPage {

	private final List<ReportErrorMessage> errors;
	private final SkippedScenarios skippedScenarios;
	private final ChangedFilesReport gdChanges;
	private final PostmanDataReport postmanData;

	public TmoFeaturesOverviewPage(final ReportResult reportResult, final Configuration configuration, List<ReportErrorMessage> errors,
	                               final SkippedScenarios skippedScenarios,
	                               final ChangedFilesReport gdChanges,
	                               final PostmanDataReport postmanData) {
		super(reportResult, configuration);
		this.errors = errors;
		this.skippedScenarios = skippedScenarios;
		this.gdChanges = gdChanges;
		this.postmanData = postmanData;
	}

	@Override
	public void prepareReport() {
		super.prepareReport();
		context.put("error_messages", errors);
		context.put("skippedScenarios", skippedScenarios);
		context.put("gdChanges", gdChanges);
		context.put("postmanData", postmanData);
	}
}
