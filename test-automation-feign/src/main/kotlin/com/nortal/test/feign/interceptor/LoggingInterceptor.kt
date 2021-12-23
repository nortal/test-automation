package com.nortal.test.feign.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException


@Component
open class LoggingInterceptor : Interceptor {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        val response = chain.proceed(request)
        val duration = System.currentTimeMillis() - startTime

        log.info("Received response with code {} for {} in {}ms.", response.code, response.request.url, duration)
        return response
    }
}