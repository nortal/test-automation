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

/**
 * This class represents an assertion that will be carried out.
 */
data class Assertion(
    /**
     * Assertion messages describing what the assertion does.
     * e.g. Verify ratePlanSoc or Verify correct line count
     */
    val message: String = "",

    /**
     * SpEL expression that retrieves actual value from the context.
     * Expectation here is that once the expression is parsed and getValue is executed on the context whatever actualValue is returned will
     * pass the selected operation against the expectedValue.
     *
     * Some default variables can be used in the expression:
     * #root to reference the root context object
     * #expected to reference the expectedValue
     */
    val expression: String = "",

    val expressionType: ExpressionType = ExpressionType.RELATIVE,

    /**
     * Operation that will be executed using expected and actual values as operands
     */
    val operation: AssertionOperation = AssertionOperation.EQUALS,

    /**
     * Expected value.
     * Depending on the selected operation may be optional (e.g. for NOT_NULL)
     */
    val expectedValue: Any? = null,

    /**
     * Context values.
     * Designed to be used in tandem with EXPRESSION
     */
    val contextValues: Map<String, Any>? = null,

    /**
     * Some assertions can act as gatekeepers, meaning that if they fail, there is little meaning in running the rest of them.
     * If TRUE will skip the rest of assertions.
     * FALSE by default
     */
    val skipRestIfFailed: Boolean = false,

    /**
     * Some assertions are just simple assertions where you would want to compare two numbers and check if they match
     * and for them to show up in the report. For this actual value can be set.
     */
    val actualValue: Any? = null
) {


    private constructor(builder: Assertion.Builder) : this(
        message = builder.message ?: throw NullPointerException(),
        expression = builder.expression ?: throw NullPointerException(),
        expressionType = builder.expressionType ?: ExpressionType.RELATIVE,
        operation = builder.operation ?: AssertionOperation.EQUALS,
        expectedValue = builder.expectedValue,
        contextValues = builder.contextValues,
        skipRestIfFailed = builder.skipRestIfFailed ?: false,
        actualValue = builder.actualValue
    )

    class Builder() {
        var message: String? = null
        var expression: String? = null
        var expressionType: ExpressionType? = null
        var operation: AssertionOperation? = null
        var expectedValue: Any? = null
        var contextValues: Map<String, Any>? = null
        var skipRestIfFailed: Boolean? = null
        var actualValue: Any? = null

        private constructor(assertion: Assertion) : this() {
            message = assertion.message
            expression = assertion.expression
            expressionType = assertion.expressionType
            operation = assertion.operation
            expectedValue = assertion.expectedValue
            contextValues = assertion.contextValues
            skipRestIfFailed = assertion.skipRestIfFailed
            actualValue = assertion.actualValue
        }

        companion object {
            @JvmStatic
            fun fromAssertion(assertion: Assertion): Builder {
                return Builder(assertion)
            }
        }

        fun message(message: String) = apply { this.message = message }

        fun expression(expression: String) = apply { this.expression = expression }

        fun expressionType(expressionType: ExpressionType) = apply { this.expressionType = expressionType }

        fun operation(operation: AssertionOperation) = apply { this.operation = operation }

        fun expectedValue(expectedValue: Any?) = apply { this.expectedValue = expectedValue }

        fun contextValues(contextValues: Map<String, Any>) = apply { this.contextValues = contextValues }

        fun skipRestIfFailed(skipRestIfFailed: Boolean) = apply { this.skipRestIfFailed = skipRestIfFailed }

        fun actualValue(actualValue: Any?) = apply { this.actualValue = actualValue }

        fun build() = Assertion(this)
    }

}