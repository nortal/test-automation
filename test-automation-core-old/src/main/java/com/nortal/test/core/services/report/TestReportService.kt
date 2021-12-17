package com.nortal.test.core.services.report

import com.nortal.test.core.services.report.cucumber.ReportFormatter.Attachment.Companion.create
import com.nortal.test.core.services.report.cucumber.ReportFormatter.Attachment.setName
import com.nortal.test.core.services.report.cucumber.ReportFormatter.Attachment.addSection
import com.nortal.test.core.services.report.cucumber.JsonFormattingUtils.prettyPrintHtmlJson
import com.nortal.test.core.services.report.cucumber.ReportFormatter
import com.nortal.test.core.services.report.cucumber.ReportFormatter.formatAndAddToReport
import com.nortal.test.core.services.report.cucumber.ReportHtmlTableGenerator.generateTable
import com.nortal.test.core.services.report.cucumber.ReportFormatter.Attachment.setTitle
import com.nortal.test.core.services.report.cucumber.ReportHtmlTableGenerator
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Component

/**
 * Service responsible for exposing methods that attach information to the test report.
 */
@Component
@RequiredArgsConstructor
class TestReportService {
    private val reportFormatter: ReportFormatter? = null
    private val reportHtmlTableGenerator: ReportHtmlTableGenerator? = null

    /**
     * Attaches the provided object to the report, formatting it as json.
     *
     * @param title   of the section
     * @param content to be attached
     */
    fun attachJson(title: String?, content: Any?) {
        val attachment = create()
            .setName(title!!)
            .addSection("", ReportFormatter.SectionType.BARE, prettyPrintHtmlJson(content))
        reportFormatter!!.formatAndAddToReport(attachment)
    }

    /**
     * Attaches the provided text to the report.
     *
     * @param title of the section
     * @param text  to be attached
     */
    fun attachText(title: String?, text: String?) {
        val attachment = create()
            .setName(title!!)
            .addSection("", ReportFormatter.SectionType.BARE, text!!)
        reportFormatter!!.formatAndAddToReport(attachment)
    }

    /**
     * Attaches the provided object to the report, formatting it as json.
     *
     * @param title   of the section
     * @param content to be attached
     */
    fun attachTable(title: String?, content: List<List<String?>?>?) {
        val attachment = create()
            .setName(title!!)
            .addSection("", ReportFormatter.SectionType.TABLE, reportHtmlTableGenerator!!.generateTable(content, true))
        reportFormatter!!.formatAndAddToReport(attachment)
    }

    /**
     * Attaches served mocks.
     *
     * @param title      title
     * @param id         id
     * @param request    request
     * @param servedMock servedMock
     * @param response   response
     */
    fun attachServedMocks(title: String?, id: String, request: String?, servedMock: String?, response: String?) {
        val attachment = create()
            .setName(title!!)
            .setTitle("Mock Id: $id")
            .addSection("Request matcher:", ReportFormatter.SectionType.COLLAPSIBLE, servedMock!!)
            .addSection("Actual request:", ReportFormatter.SectionType.COLLAPSIBLE, request!!)
            .addSection("Actual response:", ReportFormatter.SectionType.COLLAPSIBLE, response!!)
        reportFormatter!!.formatAndAddToReport(attachment)
    }

    /**
     * Attaches multiple jsons in the same attachment.
     * Separate collapsible blocks will have map keys as their titles and values as content.
     *
     * @param jsonMap map of jsons to attach
     */
    fun attachJson(attachmentTitle: String?, jsonMap: Map<String?, Any?>) {
        val attachment = create()
            .setName(attachmentTitle!!)
            .setTitle(attachmentTitle)
        jsonMap.forEach { (key: String?, `val`: Any?) ->
            attachment.addSection(
                key!!,
                ReportFormatter.SectionType.COLLAPSIBLE,
                prettyPrintHtmlJson(`val`)
            )
        }
        reportFormatter!!.formatAndAddToReport(attachment)
    }
}