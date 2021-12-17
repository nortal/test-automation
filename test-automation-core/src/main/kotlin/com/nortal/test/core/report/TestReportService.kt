package com.nortal.test.core.report

import com.nortal.test.core.report.ReportFormatter.Attachment.Companion.create
import io.cucumber.java.Scenario
import org.springframework.stereotype.Component

/**
 * Service responsible for exposing methods that attach information to the test report.
 */
@Component
class TestReportService(private val reportFormatter: ReportFormatter) {
    /**
     * Attaches the provided object to the report, formatting it as json.
     * @param title of the section
     * @param content to be attached
     */
    fun attachJson(title: String?, content: Any?) {
        val attachment = create()
            .setName(title!!)
            .addSection("", ReportFormatter.SectionType.BARE, JsonFormattingUtils.prettyPrintJson(content))
        reportFormatter.formatAndAddToReport(attachment)
    }

    /**
     * Attaches the provided object to the report, formatting it as json.
     * @param title of the section
     * @param content to be attached
     */
    fun attachJson(title: String?, content: Any?, scenario: Scenario) {
        val attachment = create()
            .setName(title!!)
            .addSection("", ReportFormatter.SectionType.BARE, JsonFormattingUtils.prettyPrintJson(content))
        reportFormatter.formatAndAddToReport(attachment, scenario)
    }

    /**
     * Attaches the provided text to the report.
     * @param title of the section
     * @param text to be attached
     */
    fun attachText(title: String?, text: String?) {
        val attachment = create()
            .setName(title!!)
            .addSection("", ReportFormatter.SectionType.BARE, text!!)
        reportFormatter.formatAndAddToReport(attachment)
    }
}