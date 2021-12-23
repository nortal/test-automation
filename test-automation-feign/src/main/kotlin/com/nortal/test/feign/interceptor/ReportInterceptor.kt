package com.nortal.test.feign.interceptor

import com.nortal.test.core.report.RequestResponseReportFormatter
import com.nortal.test.core.report.model.HttpHeaders
import com.nortal.test.core.report.model.HttpRequest
import com.nortal.test.core.report.model.HttpResponse
import com.nortal.test.feign.util.FeignUtils
import okhttp3.*
import org.springframework.stereotype.Component
import java.io.IOException

@Component
open class ReportInterceptor(
    private val formatter: RequestResponseReportFormatter
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            val response = chain.proceed(chain.request())
            formatter.formatAndAddToReport(resolveRequest(response.request), resolveResponse(response))
            response
        } catch (e: Exception) {
            formatter.formatAndAddToReport(resolveRequest(chain.request()))
            throw e
        }
    }

    private fun resolveRequest(request: Request): HttpRequest {
        val headers = request.headers.toMultimap().toMutableMap()
        if (request.body != null) {
            val mediaType = request.body!!.contentType()
            if (mediaType != null) {
                headers[org.springframework.http.HttpHeaders.CONTENT_TYPE] = listOf(mediaType.toString())
            }
        }
        return HttpRequest(
            method = request.method.uppercase(),
            url = request.url.toString(),
            headers = HttpHeaders.fromMap(headers),
            body = FeignUtils.convertRequestBodyToString(request)
        )
    }

    private fun resolveResponse(response: Response): HttpResponse {
        return HttpResponse(
            statusCode = response.code,
            headers = HttpHeaders.fromMap(response.headers.toMultimap()),
            body = resolveBody(response)
        )
    }

    private fun resolveBody(response: Response): String? {
        val body: ResponseBody = response.peekBody(Long.MAX_VALUE)
        return body.string()
    }
}