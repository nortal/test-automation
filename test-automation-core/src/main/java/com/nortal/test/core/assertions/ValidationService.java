package com.nortal.test.core.assertions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.assertj.core.util.Arrays;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static com.nortal.test.core.assertions.ValidationService.AssertionStatus.FAILED;
import static com.nortal.test.core.assertions.ValidationService.AssertionStatus.OK;
import static com.nortal.test.core.assertions.ValidationService.AssertionStatus.SKIPPED;

/**
 * Service for validating assertions and attaching them to report.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationService {
	private static final SpelExpressionParser PARSER = new SpelExpressionParser();
	private static final String EXPECTED_CONTEXT_VAR = "expected";

	private final AssertionsFormatter assertionsFormatter;

	/**
	 * Validates multiple validations and attaches them to the report.
	 *
	 * @param validations to perform
	 */
	public void validate(final List<Validation> validations) {
		final List<CompletedAssertion> completedAssertions = validations.stream()
				.map(this::doValidation)
				.flatMap(List::stream)
				.collect(Collectors.toList());

		throwIfFailed(completedAssertions);
	}

	/**
	 * Validates single validation and attaches it to the report
	 *
	 * @param validation to validate
	 */
	public void validate(final Validation validation) {
		throwIfFailed(doValidation(validation));
	}

	private List<CompletedAssertion> doValidation(final Validation validation) {
		final EvaluationContext ctx = new StandardEvaluationContext(validation.getContext());

		final List<CompletedAssertion> completedAssertions = new ArrayList<>();
		boolean skip = false;

		for (Assertion assertion : validation.getAssertions()) {
			final CompletedAssertion completedAssertion;
			if (!skip) {
				completedAssertion = doAssert(ctx, assertion, validation);
				skip = assertion.getSkipRestIfFailed() && completedAssertion.getStatus().equals(FAILED);
			} else {
				completedAssertion = new CompletedAssertion(assertion, validation.getBaseExpression(), SKIPPED, "");
			}
			completedAssertions.add(completedAssertion);
		}

		assertionsFormatter.formatAndAttachToReport(validation, completedAssertions);
		return completedAssertions;
	}

	private CompletedAssertion doAssert(final EvaluationContext ctx, final Assertion assertion, final Validation validation) {
		final String baseExpression = validation.getBaseExpression();
		final Object actualValue;
		String expression = null;
		try {
			expression = getExpression(assertion, baseExpression);
			if (assertion.getExpectedValue() != null) {
				ctx.setVariable(EXPECTED_CONTEXT_VAR, assertion.getExpectedValue());
			}
			if (assertion.getContextValues() != null) {
				assertion.getContextValues().forEach(ctx::setVariable);
			}

			if (assertion.getActualValue() != null) {
				actualValue = assertion.getActualValue();
			} else {
				actualValue = PARSER.parseExpression(expression).getValue(ctx);
			}
		} catch (ParseException | SpelEvaluationException | IllegalStateException e) {
			log.debug("Assertion failed to parse/evaluate. Expression : {}", expression, e);
			return new CompletedAssertion(assertion, baseExpression, FAILED, "Assertion failed to parse/evaluate path: " + e.getMessage());
		}

		switch (assertion.getOperation()) {
			case EQUALS:
				return assertEquals(assertion, baseExpression, actualValue);
			case NOT_EQUALS:
				return assertNotEquals(assertion, baseExpression, actualValue);
			case NULL:
				return assertNull(assertion, baseExpression, actualValue);
			case NOT_NULL:
				return assertNotNull(assertion, baseExpression, actualValue);
			case CONTAINS:
				return assertContains(assertion, baseExpression, actualValue);
			case LIST_CONTAINS:
				return assertListContains(assertion, baseExpression, actualValue);
			case LIST_CONTAINS_VALUE:
				return assertListOfExpectedValuesContainsActualValue(assertion, baseExpression, actualValue);
			case LIST_EXCLUDES:
				return assertListExcludes(assertion, baseExpression, actualValue);
			case LIST_EQUALS:
				return assertListContainsAll(assertion, baseExpression, actualValue);
			case EMPTY:
				return assertEmpty(assertion, baseExpression, actualValue);
			case EXPRESSION:
				return assertExpression(assertion, baseExpression, actualValue);
			default:
				throw new IllegalStateException("Unsupported verification operation" + assertion.getOperation());
		}
	}

	private String getExpression(final Assertion assertion, final String baseExpression) {
		if (assertion.getExpressionType().equals(Assertion.ExpressionType.RELATIVE)) {
			return baseExpression + assertion.getExpression();
		} else {
			return assertion.getExpression();
		}
	}

	private CompletedAssertion assertExpression(final Assertion assertion, final String baseExpression, final Object actualValue) {
		if (!(actualValue instanceof Boolean)) {
			return new CompletedAssertion(assertion, baseExpression, FAILED, "Expression operation returned NON boolean value");
		}
		return new CompletedAssertion(assertion, baseExpression, (Boolean) actualValue ? OK : FAILED, actualValue);
	}

	private CompletedAssertion assertContains(final Assertion assertion, final String baseExpression, final Object actualValue) {
		if (!(actualValue instanceof String)) {
			return new CompletedAssertion(assertion, baseExpression, FAILED, "Can not perform CONTAINS. Actual value not String!");
		}
		if (!(assertion.getExpectedValue() instanceof String)) {
			return new CompletedAssertion(assertion, baseExpression, FAILED, "Can not perform CONTAINS. Expected value not String!");
		}

		final String actualString = (String) actualValue;
		final String expectedString = (String) assertion.getExpectedValue();

		final boolean contains = actualString.contains(expectedString);
		return new CompletedAssertion(assertion, baseExpression, contains ? OK : FAILED, actualString);
	}

	private CompletedAssertion assertListContains(final Assertion assertion, final String baseExpression, final Object actualValue) {
		if (!(actualValue instanceof Collection)) {
			return new CompletedAssertion(assertion, baseExpression, FAILED, "Can not perform LIST CONTAINS. Actual value not Collection!");
		}
		if (!(assertion.getExpectedValue() instanceof Collection)) {
			return new CompletedAssertion(assertion, baseExpression, FAILED, "Can not perform LIST CONTAINS. Expected value not Collection!");
		}

		final Collection<Object> expectedValues = new ArrayList<>();
		for(Object o : (Collection) assertion.getExpectedValue()) {
			expectedValues.add( o);
		}
		final Collection<Object> actualValues = new ArrayList<>();
		for(Object o : (Collection) actualValue) {
			actualValues.add(o);
		}

		for (Object expectedValue : expectedValues) {
			if (!actualValues.contains(expectedValue)) {
				return new CompletedAssertion(assertion, baseExpression, FAILED, actualValues);
			}
		}

		return new CompletedAssertion(assertion, baseExpression, OK, actualValues);
	}

	private CompletedAssertion assertListOfExpectedValuesContainsActualValue(final Assertion assertion, final String baseExpression, final Object actualValue) {
		if (!(assertion.getExpectedValue() instanceof Collection)) {
			return new CompletedAssertion(assertion, baseExpression, FAILED, "Can not perform LIST CONTAINS ACTUAL VALUE. Expected value not Collection!");
		}

		final Collection<Object> expectedValues = new ArrayList<>();
		for(Object o : (Collection) assertion.getExpectedValue()) {
			expectedValues.add( o);
		}

		if (!expectedValues.contains(actualValue)) {
			return new CompletedAssertion(assertion, baseExpression, FAILED, actualValue);
		}

		return new CompletedAssertion(assertion, baseExpression, OK, actualValue);
	}

	private CompletedAssertion assertListContainsAll(final Assertion assertion, final String baseExpression, final Object actualValue) {
		if (!(actualValue instanceof Collection)) {
			return new CompletedAssertion(assertion, baseExpression, FAILED, "Can not perform LIST CONTAINS. Actual value not Collection!");
		}
		if (!(assertion.getExpectedValue() instanceof Collection)) {
			return new CompletedAssertion(assertion, baseExpression, FAILED, "Can not perform LIST CONTAINS. Expected value not Collection!");
		}

		Collection expectedValues = (Collection) assertion.getExpectedValue();
		Collection actualValues = (Collection) actualValue;

		if (actualValues.containsAll(expectedValues) && expectedValues.containsAll(actualValues)) {
			return new CompletedAssertion(assertion, baseExpression, OK, actualValues);
		}

		return new CompletedAssertion(assertion, baseExpression, FAILED, actualValues);
	}

	private CompletedAssertion assertListExcludes(final Assertion assertion, final String baseExpression, final Object actualValue) {
		if (!(actualValue instanceof List)) {
			return new CompletedAssertion(assertion, baseExpression, FAILED, "Can not perform LIST CONTAINS. Actual value not List!");
		}
		if (!(assertion.getExpectedValue() instanceof List)) {
			return new CompletedAssertion(assertion, baseExpression, FAILED, "Can not perform LIST CONTAINS. Expected value not List!");
		}

		final List<String> nonExpectedValues = new ArrayList<>();
		for(Object o : (List) assertion.getExpectedValue()) {
			nonExpectedValues.add((String) o);
		}
		final List<String> actualValues = new ArrayList<>();
		for(Object o : (List) actualValue) {
			actualValues.add((String) o);
		}

		boolean excludes = true;
		for (String nonExpectedValue: nonExpectedValues) {
			if (actualValues.contains(nonExpectedValue)) {
				excludes = false;
				break;
			}
		}

		return new CompletedAssertion(assertion, baseExpression, excludes ? OK : FAILED, actualValues);

	}

	private CompletedAssertion assertNull(final Assertion assertion, final String baseExpression, final Object actualValue) {
		final boolean exists = Objects.isNull(actualValue);
		return exists
				? new CompletedAssertion(assertion, baseExpression, OK, "NULL")
				: new CompletedAssertion(assertion, baseExpression, FAILED, "NOT_NULL");
	}

	private CompletedAssertion assertNotNull(final Assertion assertion, final String baseExpression, final Object actualValue) {
		final boolean exists = Objects.nonNull(actualValue);
		return exists
				? new CompletedAssertion(assertion, baseExpression, OK, "NOT_NULL")
				: new CompletedAssertion(assertion, baseExpression, FAILED, "NULL");
	}

	private CompletedAssertion assertEquals(final Assertion assertion, final String baseExpression, final Object actualValue) {
		final Object expectedValue = assertion.getExpectedValue();
		final boolean equals = Objects.equals(expectedValue, actualValue);
		return new CompletedAssertion(assertion, baseExpression, equals ? OK : FAILED, actualValue);
	}

	private CompletedAssertion assertNotEquals(final Assertion assertion, final String baseExpression, final Object actualValue) {
		final Object expectedValue = assertion.getExpectedValue();
		final boolean equals = Objects.equals(expectedValue, actualValue);
		return new CompletedAssertion(assertion, baseExpression, equals ? FAILED : OK, actualValue);
	}

	private CompletedAssertion assertEmpty(final Assertion assertion, final String baseExpression, final Object actualValue) {
		if (actualValue instanceof List) {
			final List<Object> objects = (List) actualValue;
			return objects.isEmpty()
					? new CompletedAssertion(assertion, baseExpression, OK, "EMPTY")
					: new CompletedAssertion(assertion, baseExpression, FAILED, "NOT_EMPTY");
		} else if (Arrays.isArray(actualValue)) {
			return Arrays.isNullOrEmpty((Object[]) actualValue)
					? new CompletedAssertion(assertion, baseExpression, OK, "EMPTY")
					: new CompletedAssertion(assertion, baseExpression, FAILED, "NOT_EMPTY");
		} else {
			throw new AssertionError("Object is not a list and cannot be compared");
		}
	}

	private void throwIfFailed(final List<CompletedAssertion> completedAssertions) {
		final List<CompletedAssertion> failedAssertions = completedAssertions.stream()
				.filter(it -> it.getStatus().equals(FAILED))
				.collect(Collectors.toList());

		if (failedAssertions.isEmpty()) {
			return;
		}

		final String message = assertionsFormatter.formatIntoErrorMessage(failedAssertions);
		throw new AssertionError("Assertions failed! Find more details in the attachment.\n" + message);
	}

	public enum AssertionStatus {
		OK, FAILED, SKIPPED
	}

	@Data
	@RequiredArgsConstructor
	public static class CompletedAssertion {

		private final Assertion assertion;

		private final String baseExpression;

		private final AssertionStatus status;

		private final Object actualValue;
	}

}
