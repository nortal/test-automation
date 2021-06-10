package com.nortal.test.core.assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.nortal.test.core.services.report.cucumber.JsonFormattingUtils;
import com.nortal.test.core.services.report.cucumber.ReportFormatter;
import com.nortal.test.core.services.report.cucumber.ReportHtmlTableGenerator;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AssertionsFormatter {

	public static final List<String> HEADERS = List.of("Status", "Message", "Actual value path", "Operation", "Expected value", "Actual value");

	private final ReportHtmlTableGenerator reportHtmlTableGenerator;
	private final ReportFormatter reportFormatter;

	public void formatAndAttachToReport(final Validation validation, final List<ValidationService.CompletedAssertion> completedAssertions) {
		final List<List<String>> assertionsTable = new ArrayList<>();
		assertionsTable.add(HEADERS);
		completedAssertions.stream()
				.map(assertion -> convertToTableLine(assertion, validation))
				.forEach(assertionsTable::add);
		final String formattedAssertionsTable = reportHtmlTableGenerator.generateTable(assertionsTable, false);

		final ReportFormatter.Attachment attachment = ReportFormatter.Attachment.create().setName(validation.getTitle());
		attachment.addSection(validation.getContextDescription(), ReportFormatter.SectionType.COLLAPSIBLE,
				JsonFormattingUtils.prettyPrintHtmlJson(validation.getContext()));
		attachment.addSection("Assertions", ReportFormatter.SectionType.TABLE, formattedAssertionsTable);
		reportFormatter.formatAndAddToReport(attachment);
	}

	private static List<String> convertToTableLine(ValidationService.CompletedAssertion completedAssertion, final Validation validation) {
		final Assertion assertion = completedAssertion.getAssertion();
		final String expectedValueString = getExpectedValue(assertion);
		final String expression = getExpression(assertion, validation.getBaseExpression());
		final String actualValueString = getActualValue(completedAssertion);

		return List.of(
				completedAssertion.getStatus().toString(),
				assertion.getMessage(),
				expression,
				assertion.getOperation().toString(),
				expectedValueString,
				actualValueString);
	}

	public String formatIntoErrorMessage(final List<ValidationService.CompletedAssertion> failedAssertions) {
		return failedAssertions.stream()
				.map(this::getExceptionMessage)
				.collect(Collectors.joining("\n"));
	}

	private String getExceptionMessage(ValidationService.CompletedAssertion completedAssertion) {
		final String expression = getExpression(completedAssertion.getAssertion(), completedAssertion.getBaseExpression());
		final String expectedValue = getExpectedValue(completedAssertion.getAssertion());
		final String actualValue = getActualValue(completedAssertion);
		return String.format(
				"Failed assertion: %s, path [%s] expected [%s] operation [%s] actual [%s]",
				completedAssertion.getAssertion().getMessage(),
				expression,
				expectedValue,
				completedAssertion.getAssertion().getOperation(),
				actualValue);
	}

	private static String getActualValue(final ValidationService.CompletedAssertion completedAssertion) {
		if (completedAssertion.getActualValue() == null) {
			return  "NULL";
		} else {
			return completedAssertion.getActualValue().toString();
		}
	}

	private static String getExpression(final Assertion assertion, final String baseExpression) {
		if (assertion.getExpressionType().equals(Assertion.ExpressionType.RELATIVE)) {
			return baseExpression + assertion.getExpression();
		} else {
			return assertion.getExpression();
		}
	}

	private static String getExpectedValue(final Assertion assertion) {
		if (assertion.getOperation().equals(Assertion.AssertionOperation.NOT_NULL)) {
			return "";
		} else {
			final Object expectedValue = assertion.getExpectedValue();
			return expectedValue == null ? "NULL" : expectedValue.toString();
		}
	}
}
