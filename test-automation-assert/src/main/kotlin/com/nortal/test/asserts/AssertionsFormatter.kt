package com.nortal.test.asserts

import com.nortal.test.core.report.JsonFormattingUtils
import com.nortal.test.core.report.ReportFormatter
import com.nortal.test.core.report.ReportFormatter.Attachment.Companion.create
import com.nortal.test.core.report.html.ReportHtmlTableGenerator
import org.springframework.stereotype.Component
import java.util.ArrayList
import java.util.stream.Collectors

@Component
open class AssertionsFormatter(
    private val reportHtmlTableGenerator: ReportHtmlTableGenerator,
    private val reportFormatter: ReportFormatter,
) {

    fun formatAndAttachToReport(validation: Validation, completedAssertions: List<ValidationService.CompletedAssertion>) {
        val assertionsTable: MutableList<List<String?>> = ArrayList()
        assertionsTable.add(HEADERS)
        completedAssertions.stream()
            .map { assertion: ValidationService.CompletedAssertion -> convertToTableLine(assertion, validation) }
            .forEach { e: List<String?> -> assertionsTable.add(e) }
        val formattedAssertionsTable = reportHtmlTableGenerator.generateTable(assertionsTable, false)
        val attachment = create().setName(validation.title)
        attachment.addSection(
            validation.contextDescription?:"", ReportFormatter.SectionType.COLLAPSIBLE,
            JsonFormattingUtils.prettyPrintHtml(validation.context)
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
            completedAssertion.assertion.message,
            expression,
            expectedValue,
            completedAssertion.assertion.operation,
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
                assertion.message,
                expression,
                assertion.operation.toString(),
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
            return if (assertion.expressionType.equals(ExpressionType.RELATIVE)) {
                baseExpression + assertion.expression
            } else {
                assertion.expression
            }
        }

        private fun getExpectedValue(assertion: Assertion): String {
            return if (assertion.operation.equals(AssertionOperation.NOT_NULL)) {
                ""
            } else {
                val expectedValue: Any? = assertion.expectedValue
                expectedValue?.toString() ?: "NULL"
            }
        }
    }
}