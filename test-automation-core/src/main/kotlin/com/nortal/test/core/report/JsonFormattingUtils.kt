package com.nortal.test.core.report

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.common.html.HtmlEscapers
import org.apache.commons.lang3.StringUtils

object JsonFormattingUtils {
    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
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