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
package com.nortal.test.asserts

import org.assertj.core.util.Arrays
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.expression.EvaluationContext
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
@Component
open class ValidationService(
    private val assertionsFormatter: AssertionsFormatter
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Validates multiple validations and attaches them to the report.
     *
     * @param validations to perform
     */
    fun validate(validations: Collection<Validation>) {
        val completedAssertions = validations
            .map { validation: Validation -> doValidation(validation) }
            .flatten()

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
        val ctx: EvaluationContext = StandardEvaluationContext(validation.context)
        val completedAssertions: MutableList<CompletedAssertion> = ArrayList()
        var skip = false
        for (assertion in validation.assertions) {
            val completedAssertion: CompletedAssertion
            if (!skip) {
                completedAssertion = doAssert(ctx, assertion, validation)
                skip = assertion.skipRestIfFailed && completedAssertion.status == AssertionStatus.FAILED
            } else {
                completedAssertion = CompletedAssertion(assertion, validation.baseExpression, AssertionStatus.SKIPPED, "")
            }
            completedAssertions.add(completedAssertion)
        }
        assertionsFormatter.formatAndAttachToReport(validation, completedAssertions)
        return completedAssertions
    }

    private fun doAssert(ctx: EvaluationContext, assertion: Assertion, validation: Validation): CompletedAssertion {
        val baseExpression: String = validation.baseExpression
        val actualValue: Any?
        try {
            actualValue = resolveActualValue(ctx, assertion, validation)
        } catch (e: Exception) {
            return CompletedAssertion(assertion, baseExpression, AssertionStatus.FAILED, "Assertion failed to parse/evaluate path: " + e.message)
        }

        return when (assertion.operation) {
            AssertionOperation.EQUALS -> assertEquals(assertion, baseExpression, actualValue)
            AssertionOperation.NOT_EQUALS -> assertNotEquals(assertion, baseExpression, actualValue)
            AssertionOperation.NULL -> assertNull(assertion, baseExpression, actualValue)
            AssertionOperation.NOT_NULL -> assertNotNull(assertion, baseExpression, actualValue)
            AssertionOperation.CONTAINS -> assertContains(assertion, baseExpression, actualValue)
            AssertionOperation.LIST_CONTAINS -> assertListContains(assertion, baseExpression, actualValue)
            AssertionOperation.LIST_CONTAINS_VALUE -> assertListOfExpectedValuesContainsActualValue(assertion, baseExpression, actualValue)
            AssertionOperation.LIST_EXCLUDES -> assertListExcludes(assertion, baseExpression, actualValue)
            AssertionOperation.LIST_EQUALS -> assertListContainsAll(assertion, baseExpression, actualValue)
            AssertionOperation.EMPTY -> assertEmpty(assertion, baseExpression, actualValue)
            AssertionOperation.EXPRESSION -> assertExpression(assertion, baseExpression, actualValue)
            else -> throw IllegalStateException("Unsupported verification operation" + assertion.operation)
        }
    }

    private fun resolveActualValue(ctx: EvaluationContext, assertion: Assertion, validation: Validation): Any? {
        var expression: String? = null
        try {
            val baseExpression: String = validation.baseExpression
            expression = getExpression(assertion, baseExpression)
            if (assertion.expectedValue != null) {
                ctx.setVariable(EXPECTED_CONTEXT_VAR, assertion.expectedValue)
            }
            if (assertion.contextValues != null) {
                assertion.contextValues.forEach { (name: String?, value: Any?) -> ctx.setVariable(name, value) }
            }

            return assertion.actualValue ?: PARSER.parseExpression(expression).getValue(ctx)
        } catch (e: Exception) {
            log.debug("Assertion failed to parse/evaluate. Expression : {}", expression, e)
            throw e
        }
    }

    private fun getExpression(assertion: Assertion, baseExpression: String): String {
        return if (assertion.expressionType.equals(ExpressionType.RELATIVE)) {
            baseExpression + assertion.expression
        } else {
            assertion.expression
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
        if (assertion.expectedValue !is String) {
            return CompletedAssertion(assertion, baseExpression, AssertionStatus.FAILED, "Can not perform CONTAINS. Expected value not String!")
        }
        val actualString = actualValue
        val expectedString = assertion.expectedValue
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
        if (assertion.expectedValue !is Collection<*>) {
            return CompletedAssertion(
                assertion,
                baseExpression,
                AssertionStatus.FAILED,
                "Can not perform LIST CONTAINS. Expected value not Collection!"
            )
        }
        val expectedValues: MutableCollection<Any> = ArrayList()
        for (o in assertion.expectedValue) {
            expectedValues.add(o!!)
        }
        val actualValues: MutableCollection<Any> = ArrayList()
        for (o in actualValue) {
            actualValues.add(o!!)
        }
        for (expectedValue in expectedValues) {
            if (!actualValues.contains(expectedValue)) {
                return CompletedAssertion(assertion, baseExpression, AssertionStatus.FAILED, actualValues)
            }
        }
        return CompletedAssertion(assertion, baseExpression, AssertionStatus.OK, actualValues)
    }

    private fun assertListOfExpectedValuesContainsActualValue(assertion: Assertion, baseExpression: String, actualValue: Any?): CompletedAssertion {
        if (assertion.expectedValue !is Collection<*>) {
            return CompletedAssertion(
                assertion,
                baseExpression,
                AssertionStatus.FAILED,
                "Can not perform LIST CONTAINS ACTUAL VALUE. Expected value not Collection!"
            )
        }
        val expectedValues: MutableCollection<Any?> = ArrayList()
        for (o in assertion.expectedValue) {
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
        if (assertion.expectedValue !is Collection<*>) {
            return CompletedAssertion(
                assertion,
                baseExpression,
                AssertionStatus.FAILED,
                "Can not perform LIST CONTAINS. Expected value not Collection!"
            )
        }
        val expectedValues = assertion.expectedValue
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
        if (assertion.expectedValue !is List<*>) {
            return CompletedAssertion(assertion, baseExpression, AssertionStatus.FAILED, "Can not perform LIST CONTAINS. Expected value not List!")
        }
        val nonExpectedValues: MutableList<String> = ArrayList()
        for (o in assertion.expectedValue) {
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
        val expectedValue: Any? = assertion.expectedValue
        val equals = expectedValue == actualValue
        return CompletedAssertion(assertion, baseExpression, if (equals) AssertionStatus.OK else AssertionStatus.FAILED, actualValue)
    }

    private fun assertNotEquals(assertion: Assertion, baseExpression: String, actualValue: Any?): CompletedAssertion {
        val expectedValue: Any? = assertion.expectedValue
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
            if (Arrays.isNullOrEmpty(actualValue as Array<*>?)) CompletedAssertion(
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
            .filter { it: CompletedAssertion -> it.status == AssertionStatus.FAILED }
            .collect(Collectors.toList())
        if (failedAssertions.isEmpty()) {
            return
        }
        val message = assertionsFormatter.formatIntoErrorMessage(failedAssertions)
        throw AssertionError("Assertions failed! Find more details in the attachment.\n$message")
    }

    enum class AssertionStatus {
        OK, FAILED, SKIPPED
    }


    data class CompletedAssertion(
        val assertion: Assertion,
        val baseExpression: String,
        val status: AssertionStatus,
        val actualValue: Any?,
    )


    companion object {
        private val PARSER = SpelExpressionParser()
        private const val EXPECTED_CONTEXT_VAR = "expected"
    }
}