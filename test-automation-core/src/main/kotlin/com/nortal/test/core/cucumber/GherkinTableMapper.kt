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
package com.nortal.test.core.cucumber

/**
 * Gherking table mapper helper.
 *
 * \[absent] 	-	designed to be used in any part of Scenario to emphasize that a value is either left absent (null) on purpose
 * or during verification it is expected to be an absent (null) value.
 *
 * [n/a] 		- 	(not applicable) designed to be used in "Examples:" part of test scenario when
 * a column is needed for some "Example" cases (rows), but not for all of them.
 * When column value is not tested at all, or it is not impacting anything in the test case - "[n/a]" should be used.
 *
 * \[blank]		-	designed to be used in any part of Scenario when an empty String value ("")
 * needs to be inputted or verified by the test case.
 *
 * \[absent] vs [n/a]
 * Both of these expressions are resolved into a "null" value, but they should be used in a way they were designed to in order
 * to keep a consistent semantic understanding of the test suite. [n/a] has a dedicated purpose which is explained above and
 * \[absent] should be prioritized over [n/a] when you want to use "null" value in a testcase and this value is expected to make an impact.
 *
 * @param <T> output class
</T> */
class GherkinTableMapper(private val sanitizedMap: Map<String, String?>) {

    companion object {
        const val ABSENT = "[absent]"
        const val NOT_AVAILABLE = "[n/a]"
        const val BLANK = "[blank]"

        @JvmStatic
        fun <T> map(
            row: Map<String, String?>,
            mappingBlock: (GherkinTableMapper) -> T
        ): T {
            val sanitizedMap = replacePlaceholderValues(row)
            val mapper = GherkinTableMapper(sanitizedMap)
            return mappingBlock(mapper)
        }

        private fun replacePlaceholderValues(row: Map<String, String?>): Map<String, String?> {
            return row.mapValues {
                when (it.value) {
                    ABSENT -> null
                    NOT_AVAILABLE -> null
                    BLANK -> ""
                    else -> it.value
                }
            }
        }
    }

    fun getString(key: String): String? {
        return sanitizedMap[key]
    }

    fun getLong(key: String): Long? {
        return sanitizedMap[key]?.toLong()
    }
}

