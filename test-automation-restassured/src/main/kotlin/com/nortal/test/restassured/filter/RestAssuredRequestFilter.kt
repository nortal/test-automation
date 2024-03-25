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
package com.nortal.test.restassured.filter

import com.nortal.test.core.report.RequestResponseReportFormatter
import com.nortal.test.core.report.model.HttpHeaders
import com.nortal.test.core.report.model.HttpRequest
import com.nortal.test.core.report.model.HttpResponse
import io.restassured.filter.FilterContext
import io.restassured.filter.OrderedFilter
import io.restassured.http.Headers
import io.restassured.response.Response
import io.restassured.response.ResponseBody
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

@Component
class RestAssuredRequestFilter(
    private val formatter: RequestResponseReportFormatter
) : OrderedFilter {

    override fun getOrder(): Int {
        return OrderedFilter.LOWEST_PRECEDENCE
    }

    override fun filter(
        requestSpec: FilterableRequestSpecification,
        responseSpec: FilterableResponseSpecification,
        ctx: FilterContext
    ): Response {
        val response = ctx.next(requestSpec, responseSpec)
        formatter.formatAndAddToReport(resolveRequest(requestSpec), resolveResponse(response))
        return response
    }

    private fun resolveRequest(requestSpec: FilterableRequestSpecification): HttpRequest {
        return HttpRequest(
            method = requestSpec.method,
            url = requestSpec.uri.toString(),
            headers = resolveHeaders(requestSpec.headers),
            body = requestSpec.getBody()
        )
    }

    private fun resolveResponse(responseSpec: Response): HttpResponse {
        return HttpResponse(
            statusCode = Integer.parseInt(responseSpec.statusCode.toString()),
            headers = resolveHeaders(responseSpec.headers),
            body = resolveBody(responseSpec.body)

        )
    }

    private fun resolveBody(body: ResponseBody<*>): String? {
        if (StringUtils.isBlank(body.asString())) {
            return null
        }

        return body.asString()
    }

    private fun resolveHeaders(headers: Headers): HttpHeaders {
        return HttpHeaders.fromMap(headers.asList()
            .groupBy { it.name }
            .mapValues { entry -> entry.value.map { it.value } })

    }
}