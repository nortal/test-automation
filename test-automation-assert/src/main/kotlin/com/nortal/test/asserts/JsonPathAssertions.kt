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
 * Helper class to create most common assertions.
 */
object JsonPathAssertions {

    /**
     * Creates assertion to check if field is not null.
     */
    @JvmStatic
    fun notNullAssertion(expression: String): Assertion {
        return Assertion.Builder()
            .message("Field is not null")
            .expression(expression)
            .expressionType(ExpressionType.JSON_PATH)
            .operation(AssertionOperation.NOT_NULL)
            .build()
    }

    /**
     * Creates equals assertion.
     */
    @JvmStatic
    fun equalsAssertion(expected: Any?, actualValuePath: String, message: String): Assertion {
        return Assertion.Builder()
            .message(message)
            .expression(actualValuePath)
            .expressionType(ExpressionType.JSON_PATH)
            .expectedValue(expected)
            .build()
    }

    /**
     * Creates equals assertion.
     */
    @JvmStatic
    fun equalsAssertion(expected: Any?, actualValuePath: String): Assertion {
        return Assertion.Builder()
            .message("Assert equals")
            .expression(actualValuePath)
            .expressionType(ExpressionType.JSON_PATH)
            .expectedValue(expected)
            .build()
    }


}