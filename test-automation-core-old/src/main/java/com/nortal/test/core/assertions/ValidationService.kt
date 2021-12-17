package com.nortal.test.core.assertions

import com.nortal.test.core.assertions.AssertionsFormatter.formatAndAttachToReport
import com.nortal.test.core.assertions.AssertionsFormatter.formatIntoErrorMessage
import lombok.Data
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.assertj.core.util.Arrays
import org.springframework.expression.EvaluationContext
import org.springframework.expression.ParseException
import org.springframework.expression.spel.SpelEvaluationException
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component
import java.lang.IllegalStateException
import java.lang.AssertionError
import java.util.*
import java.util.stream.Collectors

/**
 * Service for validating assertions and attaching them to report.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class ValidationService {
    private val assertionsFormatter: AssertionsFormatter? = null

    /**
     * Validates multiple validations and attaches them to the report.
     *
     * @param validations to perform
     */
    fun validate(validations: List<Validation>) {
        val completedAssertions = validations.stream()
            .map { validation: Validation -> doValidation(validation) }
            .flatMap { obj: List<CompletedAssertion> -> obj.stream() }
            .collect(Collectors.toList())
        throwIfFailed(completedAssertions)
    }

    /**
     * Validates single validation and attaches it to the report
     *
     * @param validation to validate
     */
    fun validate(validation: Validation) {
        throwIfFailed(doValidation(validation))
    }

    private fun doValidation(validation: Validation): List<CompletedAssertion> {
        val ctx: EvaluationContext = StandardEvaluationContext(validation.getContext())
        val completedAssertions: MutableList<CompletedAssertion> = ArrayList()
        var skip = false
        for (assertion in validation.getAssertions()) {
            val completedAssertion: CompletedAssertion
            if (!skip) {
                completedAssertion = doAssert(ctx, assertion, validation)
                skip = assertion.getSkipRestIfFailed() && completedAssertion.getStatus() == AssertionStatus.FAILED
            } else {
                completedAssertion = CompletedAssertion(assertion, validation.getBaseExpression(), AssertionStatus.SKIPPED, "")
            }
            completedAssertions.add(completedAssertion)
        }
        assertionsFormatter!!.formatAndAttachToReport(validation, completedAssertions)
        return completedAssertions
    }

    private fun doAssert(ctx: EvaluationContext, assertion: Assertion, validation: Validation): CompletedAssertion {
        val baseExpression: String = validation.getBaseExpression()
        val actualValue: Any?
        var expression: String? = null
        try {
            expression = getExpression(assertion, baseExpression)
            if (assertion.getExpectedValue() != null) {
                ctx.setVariable(EXPECTED_CONTEXT_VAR, assertion.getExpectedValue())
            }
            if (assertion.getContextValues() != null) {
                assertion.getContextValues().forEach { name: String?, value: Any? -> ctx.setVariable(name!!, value) }
            }
            if (assertion.getActualValue() != null) {
                actualValue = assertion.getActualValue()
            } else {
                actualValue = PARSER.parseExpression(expression).getValue(ctx)
            }
        } catch (e: ParseException) {
            ValidationService.log.debug("Assertion failed to parse/evaluate. Expression : {}", expression, e)
            return CompletedAssertion(assertion, baseExpression, AssertionStatus.FAILED, "Assertion failed to parse/evaluate path: " + e.message)
        } catch (e: SpelEvaluationException) {
            ValidationService.log.debug("Assertion failed to parse/evaluate. Expression : {}", expression, e)
            return CompletedAssertion(assertion, baseExpression, AssertionStatus.FAILED, "Assertion failed to parse/evaluate path: " + e.message)
        } catch (e: IllegalStateException) {
            ValidationService.log.debug("Assertion failed to parse/evaluate. Expression : {}", expression, e)
            return CompletedAssertion(assertion, baseExpression, AssertionStatus.FAILED, "Assertion failed to parse/evaluate path: " + e.message)
        }
        return when (assertion.getOperation()) {
            EQUALS -> assertEquals(assertion, baseExpression, actualValue)
            NOT_EQUALS -> assertNotEquals(assertion, baseExpression, actualValue)
            NULL -> assertNull(assertion, baseExpression, actualValue)
            NOT_NULL -> assertNotNull(assertion, baseExpression, actualValue)
            CONTAINS -> assertContains(assertion, baseExpression, actualValue)
            LIST_CONTAINS -> assertListContains(assertion, baseExpression, actualValue)
            LIST_CONTAINS_VALUE -> assertListOfExpectedValuesContainsActualValue(assertion, baseExpression, actualValue)
            LIST_EXCLUDES -> assertListExcludes(assertion, baseExpression, actualValue)
            LIST_EQUALS -> assertListContainsAll(assertion, baseExpression, actualValue)
            EMPTY -> assertEmpty(assertion, baseExpression, actualValue)
            EXPRESSION -> assertExpression(assertion, baseExpression, actualValue)
            else -> throw IllegalStateException("Unsupported verification operation" + assertion.getOperation())
        }
    }

    private fun getExpression(assertion: Assertion, baseExpression: String): String {
        return if (assertion.getExpressionType().equals(Assertion.ExpressionType.RELATIVE)) {
            baseExpression + assertion.getExpression()
        } else {
            assertion.getExpression()
        }
    }

    private fun assertExpression(assertion: Assertion, baseExpression: String, actualValue: Any?): CompletedAssertion {
        return if (actualValue !is Boolean) {
            CompletedAssertion(
                assertion,
                baseExpression,
                AssertionStatus.FAILED,
                "Expression operation returned NON boolean value"
            )
        } else CompletedAssertion(
            assertion,
            baseExpression,
            if (actualValue) AssertionStatus.OK else AssertionStatus.FAILED,
            actualValue
        )
    }

    private fun assertContains(assertion: Assertion, baseExpression: String, actualValue: Any?): CompletedAssertion {
        if (actualValue !is String) {
            return CompletedAssertion(assertion, baseExpression, AssertionStatus.FAILED, "Can not perform CONTAINS. Actual value not String!")
        }
        if (assertion.getExpectedValue() !is String) {
            return CompletedAssertion(assertion, baseExpression, AssertionStatus.FAILED, "Can not perform CONTAINS. Expected value not String!")
        }
        val actualString = actualValue
        val expectedString = assertion.getExpectedValue() as String
        val contains = actualString.contains(expectedString)
        return CompletedAssertion(assertion, baseExpression, if (contains) AssertionStatus.OK else AssertionStatus.FAILED, actualString)
    }

    private fun assertListContains(assertion: Assertion, baseExpression: String, actualValue: Any?): CompletedAssertion {
        if (actualValue !is Collection<*>) {
            return CompletedAssertion(
                assertion,
                baseExpression,
                AssertionStatus.FAILED,
                "Can not perform LIST CONTAINS. Actual value not Collection!"
            )
        }
        if (assertion.getExpectedValue() !is Collection<*>) {
            return CompletedAssertion(
                assertion,
                baseExpression,
                AssertionStatus.FAILED,
                "Can not perform LIST CONTAINS. Expected value not Collection!"
            )
        }
        val expectedValues: MutableCollection<Any> = ArrayList()
        for (o in assertion.getExpectedValue() as Collection<*>) {
            expectedValues.add(o)
        }
        val actualValues: MutableCollection<Any> = ArrayList()
        for (o in actualValue) {
            actualValues.add(o)
        }
        for (expectedValue in expectedValues) {
            if (!actualValues.contains(expectedValue)) {
                return CompletedAssertion(assertion, baseExpression, AssertionStatus.FAILED, actualValues)
            }
        }
        return CompletedAssertion(assertion, baseExpression, AssertionStatus.OK, actualValues)
    }

    private fun assertListOfExpectedValuesContainsActualValue(assertion: Assertion, baseExpression: String, actualValue: Any?): CompletedAssertion {
        if (assertion.getExpectedValue() !is Collection<*>) {
            return CompletedAssertion(
                assertion,
                baseExpression,
                AssertionStatus.FAILED,
                "Can not perform LIST CONTAINS ACTUAL VALUE. Expected value not Collection!"
            )
        }
        val expectedValues: MutableCollection<Any?> = ArrayList()
        for (o in assertion.getExpectedValue() as Collection<*>) {
            expectedValues.add(o)
        }
        return if (!expectedValues.contains(actualValue)) {
            CompletedAssertion(assertion, baseExpression, AssertionStatus.FAILED, actualValue)
        } else CompletedAssertion(assertion, baseExpression, AssertionStatus.OK, actualValue)
    }

    private fun assertListContainsAll(assertion: Assertion, baseExpression: String, actualValue: Any?): CompletedAssertion {
        if (actualValue !is Collection<*>) {
            return CompletedAssertion(
                assertion,
                baseExpression,
                AssertionStatus.FAILED,
                "Can not perform LIST CONTAINS. Actual value not Collection!"
            )
        }
        if (assertion.getExpectedValue() !is Collection<*>) {
            return CompletedAssertion(
                assertion,
                baseExpression,
                AssertionStatus.FAILED,
                "Can not perform LIST CONTAINS. Expected value not Collection!"
            )
        }
        val expectedValues = assertion.getExpectedValue() as Collection<*>
        val actualValues = actualValue
        return if (actualValues.containsAll(expectedValues) && expectedValues.containsAll(actualValues)) {
            CompletedAssertion(
                assertion,
                baseExpression,
                AssertionStatus.OK,
                actualValues
            )
        } else CompletedAssertion(
            assertion,
            baseExpression,
            AssertionStatus.FAILED,
            actualValues
        )
    }

    private fun assertListExcludes(assertion: Assertion, baseExpression: String, actualValue: Any?): CompletedAssertion {
        if (actualValue !is List<*>) {
            return CompletedAssertion(assertion, baseExpression, AssertionStatus.FAILED, "Can not perform LIST CONTAINS. Actual value not List!")
        }
        if (assertion.getExpectedValue() !is List<*>) {
            return CompletedAssertion(assertion, baseExpression, AssertionStatus.FAILED, "Can not perform LIST CONTAINS. Expected value not List!")
        }
        val nonExpectedValues: MutableList<String> = ArrayList()
        for (o in assertion.getExpectedValue() as List<*>) {
            nonExpectedValues.add(o as String)
        }
        val actualValues: MutableList<String> = ArrayList()
        for (o in actualValue) {
            actualValues.add(o as String)
        }
        var excludes = true
        for (nonExpectedValue in nonExpectedValues) {
            if (actualValues.contains(nonExpectedValue)) {
                excludes = false
                break
            }
        }
        return CompletedAssertion(assertion, baseExpression, if (excludes) AssertionStatus.OK else AssertionStatus.FAILED, actualValues)
    }

    private fun assertNull(assertion: Assertion, baseExpression: String, actualValue: Any?): CompletedAssertion {
        val exists = Objects.isNull(actualValue)
        return if (exists) CompletedAssertion(assertion, baseExpression, AssertionStatus.OK, "NULL") else CompletedAssertion(
            assertion,
            baseExpression,
            AssertionStatus.FAILED,
            "NOT_NULL"
        )
    }

    private fun assertNotNull(assertion: Assertion, baseExpression: String, actualValue: Any?): CompletedAssertion {
        val exists = Objects.nonNull(actualValue)
        return if (exists) CompletedAssertion(assertion, baseExpression, AssertionStatus.OK, "NOT_NULL") else CompletedAssertion(
            assertion,
            baseExpression,
            AssertionStatus.FAILED,
            "NULL"
        )
    }

    private fun assertEquals(assertion: Assertion, baseExpression: String, actualValue: Any?): CompletedAssertion {
        val expectedValue: Any = assertion.getExpectedValue()
        val equals = expectedValue == actualValue
        return CompletedAssertion(assertion, baseExpression, if (equals) AssertionStatus.OK else AssertionStatus.FAILED, actualValue)
    }

    private fun assertNotEquals(assertion: Assertion, baseExpression: String, actualValue: Any?): CompletedAssertion {
        val expectedValue: Any = assertion.getExpectedValue()
        val equals = expectedValue == actualValue
        return CompletedAssertion(assertion, baseExpression, if (equals) AssertionStatus.FAILED else AssertionStatus.OK, actualValue)
    }

    private fun assertEmpty(assertion: Assertion, baseExpression: String, actualValue: Any?): CompletedAssertion {
        return if (actualValue is List<*>) {
            if (actualValue.isEmpty()) CompletedAssertion(
                assertion,
                baseExpression,
                AssertionStatus.OK,
                "EMPTY"
            ) else CompletedAssertion(
                assertion,
                baseExpression,
                AssertionStatus.FAILED,
                "NOT_EMPTY"
            )
        } else if (Arrays.isArray(actualValue)) {
            if (Arrays.isNullOrEmpty(actualValue as Array<Any>?)) CompletedAssertion(
                assertion,
                baseExpression,
                AssertionStatus.OK,
                "EMPTY"
            ) else CompletedAssertion(
                assertion,
                baseExpression,
                AssertionStatus.FAILED,
                "NOT_EMPTY"
            )
        } else {
            throw AssertionError("Object is not a list and cannot be compared")
        }
    }

    private fun throwIfFailed(completedAssertions: List<CompletedAssertion>) {
        val failedAssertions = completedAssertions.stream()
            .filter { it: CompletedAssertion -> it.getStatus() == AssertionStatus.FAILED }
            .collect(Collectors.toList())
        if (failedAssertions.isEmpty()) {
            return
        }
        val message = assertionsFormatter!!.formatIntoErrorMessage(failedAssertions)
        throw AssertionError("Assertions failed! Find more details in the attachment.\n$message")
    }

    enum class AssertionStatus {
        OK, FAILED, SKIPPED
    }

    @Data
    @RequiredArgsConstructor
    class CompletedAssertion {
        private val assertion: Assertion? = null
        private val baseExpression: String? = null
        private val status: AssertionStatus? = null
        private val actualValue: Any? = null
    }

    companion object {
        private val PARSER = SpelExpressionParser()
        private const val EXPECTED_CONTEXT_VAR = "expected"
    }
}