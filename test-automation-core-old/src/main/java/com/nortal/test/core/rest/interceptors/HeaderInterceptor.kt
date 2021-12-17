package com.nortal.test.core.rest.interceptors

import com.nortal.test.core.services.ScenarioContainer
import com.nortal.test.core.services.ScenarioContainer.get
import okhttp3.Interceptor
import okhttp3.Response
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class HeaderInterceptor(@param:Lazy private val scenarioContainer: ScenarioContainer) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return if (!scenarioContainer.get().common.headers.isEmpty()) {
            setHeaderValues(chain)
        } else chain.proceed(chain.request())
    }

    @Throws(IOException::class)
    private fun setHeaderValues(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        scenarioContainer.get().common.headers.forEach { (name: String?, value: String?) ->
            request.header(
                name!!, value!!
            )
        }
        return chain.proceed(request.build())
    }
}