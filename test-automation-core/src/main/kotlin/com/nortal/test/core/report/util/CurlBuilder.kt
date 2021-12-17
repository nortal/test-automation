package com.nortal.test.core.report.util

import com.nortal.test.core.report.JsonFormattingUtils
import com.nortal.test.core.report.model.HttpHeader
import com.nortal.test.core.report.model.HttpRequest
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component
class CurlBuilder {
    fun getRequestCurl(request: HttpRequest): String {
        val curl = StringBuilder("curl -X ")
            .append(request.method.toUpperCase())
            .append(" \\\n'")
            .append(request.url)
            .append("' \\\n")
        request.headers.asMap()
            .forEach { (headerKey: String, header: HttpHeader) ->
                header.values.forEach(
                    Consumer { headerValue: String -> appendHeader(curl, headerKey, headerValue) })
            }
        if (request.body != null) {
            val json = JsonFormattingUtils.prettyPrintJson(request.body)
            curl.append("-d '").append(json).append('\'')
        }
        curl.append('\n')

        //TODO verify query params
        return curl.toString()
    }

    private fun appendHeader(curl: StringBuilder, headerKey: String, headerValue: String) {
        curl.append("-H '").append(headerKey).append(": ").append(headerValue).append("' \\\n")
    }

}