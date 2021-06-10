package com.nortal.test.testcontainers.api.interceptor

import com.nortal.test.services.testcontainers.TestContainerService
import okhttp3.Interceptor
import okhttp3.Response
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class HostSelectorInterceptor(private val testContainerService: TestContainerService) : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    var request = chain.request()
    val newUrl = request.url().newBuilder()
        .port(testContainerService.exposedContainerPort)
        .build()
    request = request.newBuilder()
        .url(newUrl)
        .build()
    return chain.proceed(request)
  }
}
