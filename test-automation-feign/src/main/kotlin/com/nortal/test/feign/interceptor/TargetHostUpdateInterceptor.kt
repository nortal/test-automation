package com.nortal.test.feign.interceptor

import com.nortal.test.core.services.TestableApplicationInfoProvider
import okhttp3.Interceptor
import okhttp3.Response
import org.springframework.stereotype.Component
import java.io.IOException

@Component
open class TargetHostUpdateInterceptor(private val testableApplicationInfoProvider: TestableApplicationInfoProvider) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val newUrl = request.url.newBuilder()
            .host(testableApplicationInfoProvider.getHost())
            .port(testableApplicationInfoProvider.getPort())
            .build()

        request = request.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(request)
    }
}
