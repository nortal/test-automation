package com.nortal.test.core.rest.interceptors

import lombok.extern.slf4j.Slf4j
import okhttp3.Interceptor
import okhttp3.Response
import org.springframework.stereotype.Component
import java.io.IOException

@Slf4j
@Component
class LoggingInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        val response = chain.proceed(request)
        val duration = System.currentTimeMillis() - startTime
        LoggingInterceptor.log.info("Received response with code {} for {} in {}ms.", response.code(), response.request().url(), duration)
        return response
    }
}