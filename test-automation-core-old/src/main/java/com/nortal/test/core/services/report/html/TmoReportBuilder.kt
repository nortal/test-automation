package com.nortal.test.core.services.report.html

import com.nortal.test.core.services.report.cucumber.SkippedScenarios
import net.masterthought.cucumber.Configuration
import net.masterthought.cucumber.Trends

/**
 * TODO remove
 * This is customized report builder.
 */
class TmoReportBuilder(
    jsonFiles: List<String?>?, configuration: Configuration?,
    private val reportBaseUrl: String, private val capabilities: List<String>,
    private val skippedScenarios: SkippedScenarios,
    private val gdChanges: ChangedFilesReport
) : ReportBuilder(jsonFiles, configuration) {
    override fun copyStaticResources() {
        super.copyStaticResources()

        //compiled json react app that contains https://www.npmjs.com/package/react-json-view component for json viewing
        //component can be accessed by embedding a div with "<div class="json-view" data-input-json="BASE64_JSON"/>" into the report html
        //see main.c5f307ba.chunk.js if you want to make changes
        //while small changes can be made directly in js if you need to rebuild the whole app
        //it is attached in ReactJsonProjectForRebuildingBundledJs.zip
        copyResources("js", "2.c99a6472.chunk.js")
        copyResources("js", "main.c5f307ba.chunk.js")
        copyResources("js", "runtime-main.994def0d.js")
    }

    override fun generatePages(trends: Trends) {
        TmoFeaturesOverviewPage(reportResult, configuration, skippedScenarios, gdChanges).generatePage()
        super.generatePages(trends)
        MailSummaryPage(
            reportResult, configuration, reportBaseUrl, capabilities, skippedScenarios,
            gdChanges
        ).generatePage()
    }
}