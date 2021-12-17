package com.nortal.test.testcontainers.api.interceptor

import com.nortal.test.testcontainers.TestContainerService
import okhttp3.Interceptor
import okhttp3.Response
import org.springframework.stereotype.Component
import java.io.IOException

//TODO this is part of retrofit
@Component
class HostSelectorInterceptor(private val testContainerService: TestContainerService) : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    var request = chain.request()
    val newUrl = request.url().newBuilder()
        .port(testContainerService.getPort())
        .build()
    request = request.newBuilder()
        .url(newUrl)
        .build()
    return chain.proceed(request)
  }
}
