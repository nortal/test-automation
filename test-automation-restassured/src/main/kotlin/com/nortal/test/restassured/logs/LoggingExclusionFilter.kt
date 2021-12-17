package com.nortal.test.restassured.logs

import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.filter.FilterContext
import com.nortal.test.restassured.logs.LoggingExclusionFilter
import io.restassured.filter.OrderedFilter
import io.restassured.response.Response
import org.springframework.stereotype.Component

@Component
class LoggingExclusionFilter : OrderedFilter {
    override fun getOrder(): Int {
        return OrderedFilter.LOWEST_PRECEDENCE
    }

    override fun filter(
        requestSpec: FilterableRequestSpecification,
        responseSpec: FilterableResponseSpecification,
        ctx: FilterContext
    ): Response {
        val httpClient = requestSpec.httpClient
        httpClient.params.setParameter(SHOULD_EXLUDE_BODY_LOGGING, false)
        return ctx.next(requestSpec, responseSpec)
    }

    companion object {
        const val SHOULD_EXLUDE_BODY_LOGGING = "log"
    }
}