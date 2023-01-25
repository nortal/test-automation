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
package com.nortal.test.core.report

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.common.html.HtmlEscapers
import org.apache.commons.lang3.StringUtils

object JsonFormattingUtils {
    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModules(JavaTimeModule(), ResourceSerializingModule())
        .enable(SerializationFeature.INDENT_OUTPUT)

    @JvmStatic
    fun prettyPrintJson(body: String): String {
        return try {
            if (StringUtils.isBlank(body)) {
                body
            } else HtmlEscapers.htmlEscaper().escape(body)
        } catch (e: Exception) {
            throw AssertionError("Failed to prettify body", e)
        }
    }

    @JvmStatic
    fun prettyPrintJson(body: Any?): String {
        return try {
            HtmlEscapers.htmlEscaper().escape(objectMapper.writeValueAsString(body))
        } catch (e: RuntimeException) {
            throw AssertionError("Failed to prettify body", e)
        }
    }

    @JvmStatic
    fun prettyPrintHtml(body: Any?): String {
        return "<code class=\"json\">" + prettyPrintJson(body) + "</code>"
    }
}