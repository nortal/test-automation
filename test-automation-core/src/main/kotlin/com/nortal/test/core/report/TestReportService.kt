/**
 * Copyright (c) 2022 Nortal AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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