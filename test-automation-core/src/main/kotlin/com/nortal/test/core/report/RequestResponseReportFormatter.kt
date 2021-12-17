package com.nortal.test.core.report

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.ObjectMapper
import com.nortal.test.core.report.model.HttpRequest
import com.nortal.test.core.report.model.HttpResponse
import com.nortal.test.core.report.util.CurlBuilder
import com.nortal.test.core.report.util.HttpContentTypeUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component

/**
 * TODO: class  requires improvements
 */
@Component
open class RequestResponseReportFormatter(
    private val objectMapper: ObjectMapper,
    private val curlBuilder: CurlBuilder,
    private val formatter: ReportFormatter
) {

    /**
     * Formats the request and response and adds it to the report.
     *
     * @param request request
     * @param response    response
     */
    fun formatAndAddToReport(request: HttpRequest, response: HttpResponse) {
        val attachment = populateRequestSection(ReportFormatter.Attachment.create(), request)
            .addSection(
                "Response:",
                ReportFormatter.SectionType.COLLAPSIBLE,
                getResponse(response)
            )

        addAdditionalSections(request, response, attachment)
        formatter.formatAndAddToReport(attachment)
    }

    open fun addAdditionalSections(request: HttpRequest, response: HttpResponse, attachment: ReportFormatter.Attachment) {
        //do nothing by default.
    }

    fun formatAndAddToReport(requestSpec: HttpRequest) {
        val attachment = populateRequestSection(ReportFormatter.Attachment.create(), requestSpec)
        formatter.formatAndAddToReport(attachment)
    }

    private fun populateRequestSection(
        attachment: ReportFormatter.Attachment,
        requestSpec: HttpRequest
    ): ReportFormatter.Attachment {
        attachment
            .setName(requestSpec.method.toUpperCase() + " " + requestSpec.url)
            .setTitle(getRequestDescription(requestSpec))
            .addSection("Request:", ReportFormatter.SectionType.STANDARD, curlBuilder.getRequestCurl(requestSpec))
        return attachment
    }


    private fun getResponse(response: HttpResponse): String {
        val responseMap: MutableMap<String, Any> = HashMap()
        responseMap["status"] = response.statusCode
        responseMap["headers"] = response.headers

        val bodyStr = response.body
        val contentType = getContentType(response)
        if (StringUtils.isNotBlank(bodyStr)) {
            if (isJsonContentType(contentType)) {
                responseMap["body"] = resolveJsonBody(bodyStr!!)
            }
        } else if (HttpContentTypeUtils.isXmlContentType(contentType)) {
            responseMap["body"] = resolveXmlBody(bodyStr!!)
        }else{
            responseMap["body"]= "Endpoint returned unknown content type, not putting it to the report"
        }
        return JsonFormattingUtils.prettyPrintHtml(responseMap)
    }

    protected open fun isJsonContentType(contentType: String?): Boolean {
        return HttpContentTypeUtils.isJsonContentType(contentType)
    }

    protected open fun resolveJsonBody(body: String): Any {
        return try {
            objectMapper.readTree(body)
        } catch (ex: JacksonException) {
            body
        }
    }

    protected open fun resolveXmlBody(body: String): Any {
        //TODO for now returning as is.
        return body
    }

    private fun getContentType(response: HttpResponse): String? {
        return response.headers.getFirstByName(HttpHeaders.CONTENT_TYPE)
    }


    private fun getRequestDescription(requestSpec: HttpRequest): String {
        return String.format("Rest call: %s", requestSpec.url)
    }


}