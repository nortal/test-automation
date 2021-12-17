package com.nortal.test.core.rest.interceptors

import com.nortal.test.core.services.report.cucumber.RequestResponseReportFormatter
import com.nortal.test.core.services.report.cucumber.RequestResponseReportFormatter.formatAndAddToReport
import lombok.RequiredArgsConstructor
import okhttp3.Interceptor
import okhttp3.Response
import org.springframework.stereotype.Component
import java.io.IOException
import java.lang.Exception

@Component
@RequiredArgsConstructor
class ReportInterceptor : Interceptor {
    private val formatter: RequestResponseReportFormatter? = null
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            val response = chain.proceed(chain.request())
            formatter!!.formatAndAddToReport(response.request(), response)
            response
        } catch (e: Exception) {
            formatter!!.formatAndAddToReport(chain.request())
            throw e
        }
    }
}