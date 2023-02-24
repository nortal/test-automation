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

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest(classes = [ValidationService::class])
class ValidationServiceTest {
    @MockBean
    lateinit var assertionsFormatter: AssertionsFormatter

    @Autowired
    lateinit var validationService: ValidationService

    val response = """
    {
      "headers" : { },
      "body" : {
        "status" : 400,
        "error" : {
          "code" : "weak_pin",
          "metadata" : [ "pin_min_length", "10", "pin_min_char_classes_count", "3" ]
        }
      },
      "status" : 400
    }
    """.trimIndent()

    @Test
    fun `should parse jsonpath when expression type is set`() {
        validationService.validate(
            Validation.Builder()
                .context(response)
                .assertion(JsonPathAssertions.equalsAssertion(400,"$.status"))
                .assertion(JsonPathAssertions.equalsAssertion(4,"$.body.error.metadata.length()"))
                .assertion(
                    Assertion.Builder()
                        .expression("$.body.error.code")
                        .expectedValue("weak_pin")
                        .expressionType(ExpressionType.JSON_PATH)
                        .operation(AssertionOperation.EQUALS)
                        .build())
                .build()
        )
    }

    @Test
    fun `should parse jsonpath`() {
        validationService.validate(
            Validation.Builder()
                .context(response)
                .assertion(
                    Assertion.Builder()
                        .expression("#jsonPath(#root,'$.body.error.code')")
                        .expectedValue("weak_pin")
                        .operation(AssertionOperation.EQUALS)
                        .build()
                ).build()
        )
    }
}