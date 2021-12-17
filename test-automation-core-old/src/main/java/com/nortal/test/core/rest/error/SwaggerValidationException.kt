package com.nortal.test.core.rest.error

import com.atlassian.oai.validator.report.ValidationReport
import java.lang.StringBuilder

/**
 * Exception representing a swagger validation failure.
 */
class SwaggerValidationException(val messages: List<ValidationReport.Message>) : ValidationException(
    messagesToString(
        messages
    )
) {

    companion object {
        private const val serialVersionUID = 924828565647941758L
        protected fun messagesToString(messages: List<ValidationReport.Message>): String {
            val builder = StringBuilder("Validation failed:")
            for (message in messages) {
                builder.append('\n')
                builder.append(message.toString())
            }
            return builder.toString()
        }
    }
}