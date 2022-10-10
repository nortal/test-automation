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
) : FeignClientInterceptor {

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

    override fun getOrder(): Int {
        return 100
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