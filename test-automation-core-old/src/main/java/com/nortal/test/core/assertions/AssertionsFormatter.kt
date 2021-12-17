package com.nortal.test.core.assertions

import com.nortal.test.core.services.report.cucumber.ReportHtmlTableGenerator.generateTable
import com.nortal.test.core.services.report.cucumber.ReportFormatter.Attachment.Companion.create
import com.nortal.test.core.services.report.cucumber.ReportFormatter.Attachment.setName
import com.nortal.test.core.services.report.cucumber.ReportFormatter.Attachment.addSection
import com.nortal.test.core.services.report.cucumber.JsonFormattingUtils.prettyPrintHtmlJson
import com.nortal.test.core.services.report.cucumber.ReportFormatter
import com.nortal.test.core.services.report.cucumber.ReportFormatter.formatAndAddToReport
import com.nortal.test.core.services.report.cucumber.ReportHtmlTableGenerator
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Component
import java.util.ArrayList
import java.util.stream.Collectors

@Component
@RequiredArgsConstructor
class AssertionsFormatter {
    private val reportHtmlTableGenerator: ReportHtmlTableGenerator? = null
    private val reportFormatter: ReportFormatter? = null
    fun formatAndAttachToReport(validation: Validation, completedAssertions: List<ValidationService.CompletedAssertion>) {
        val assertionsTable: MutableList<List<String?>> = ArrayList()
        assertionsTable.add(HEADERS)
        completedAssertions.stream()
            .map { assertion: ValidationService.CompletedAssertion -> convertToTableLine(assertion, validation) }
            .forEach { e: List<String?> -> assertionsTable.add(e) }
        val formattedAssertionsTable = reportHtmlTableGenerator!!.generateTable(assertionsTable, false)
        val attachment = create().setName(validation.title)
        attachment.addSection(
            validation.contextDescription, ReportFormatter.SectionType.COLLAPSIBLE,
            prettyPrintHtmlJson(validation.context)
        )
        attachment.addSection("Assertions", ReportFormatter.SectionType.TABLE, formattedAssertionsTable)
        reportFormatter!!.formatAndAddToReport(attachment)
    }

    fun formatIntoErrorMessage(failedAssertions: List<ValidationService.CompletedAssertion>): String {
        return failedAssertions.stream()
            .map { completedAssertion: ValidationService.CompletedAssertion -> getExceptionMessage(completedAssertion) }
            .collect(Collectors.joining("\n"))
    }

    private fun getExceptionMessage(completedAssertion: ValidationService.CompletedAssertion): String {
        val expression = getExpression(completedAssertion.assertion, completedAssertion.baseExpression)
        val expectedValue = getExpectedValue(completedAssertion.assertion)
        val actualValue = getActualValue(completedAssertion)
        return java.lang.String.format(
            "Failed assertion: %s, path [%s] expected [%s] operation [%s] actual [%s]",
            completedAssertion.assertion.getMessage(),
            expression,
            expectedValue,
            completedAssertion.assertion.getOperation(),
            actualValue
        )
    }

    companion object {
        val HEADERS = java.util.List.of("Status", "Message", "Actual value path", "Operation", "Expected value", "Actual value")
        private fun convertToTableLine(completedAssertion: ValidationService.CompletedAssertion, validation: Validation): List<String?> {
            val assertion = completedAssertion.assertion
            val expectedValueString = getExpectedValue(assertion)
            val expression = getExpression(assertion, validation.baseExpression)
            val actualValueString = getActualValue(completedAssertion)
            return java.util.List.of(
                completedAssertion.status.toString(),
                assertion.getMessage(),
                expression,
                assertion.getOperation().toString(),
                expectedValueString,
                actualValueString
            )
        }

        private fun getActualValue(completedAssertion: ValidationService.CompletedAssertion): String {
            return if (completedAssertion.actualValue == null) {
                "NULL"
            } else {
                completedAssertion.actualValue.toString()
            }
        }

        private fun getExpression(assertion: Assertion, baseExpression: String): String {
            return if (assertion.getExpressionType().equals(Assertion.ExpressionType.RELATIVE)) {
                baseExpression + assertion.getExpression()
            } else {
                assertion.getExpression()
            }
        }

        private fun getExpectedValue(assertion: Assertion): String {
            return if (assertion.getOperation().equals(Assertion.AssertionOperation.NOT_NULL)) {
                ""
            } else {
                val expectedValue: Any = assertion.getExpectedValue()
                expectedValue?.toString() ?: "NULL"
            }
        }
    }
}