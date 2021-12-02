package com.nortal.test.core.services.report.html;

import com.nortal.test.core.services.report.cucumber.SkippedScenarios;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportResult;
import net.masterthought.cucumber.generators.FeaturesOverviewPage;

public class TmoFeaturesOverviewPage extends FeaturesOverviewPage {

	private final SkippedScenarios skippedScenarios;
	private final ChangedFilesReport gdChanges;

	public TmoFeaturesOverviewPage(final ReportResult reportResult, final Configuration configuration,
			final SkippedScenarios skippedScenarios,
			final ChangedFilesReport gdChanges) {
		super(reportResult, configuration);
		this.skippedScenarios = skippedScenarios;
		this.gdChanges = gdChanges;
	}

	@Override
	public void prepareReport() {
		super.prepareReport();
		context.put("skippedScenarios", skippedScenarios);
		context.put("gdChanges", gdChanges);
	}
}
