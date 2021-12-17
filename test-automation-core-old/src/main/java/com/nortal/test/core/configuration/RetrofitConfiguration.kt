package com.nortal.test.core.configuration

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.nortal.test.core.rest.interceptors.HeaderInterceptor
import com.nortal.test.core.rest.interceptors.LoggingInterceptor
import com.nortal.test.core.rest.interceptors.ReportInterceptor
import lombok.SneakyThrows
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.IOException
import java.lang.NumberFormatException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import java.util.*
import java.util.function.Consumer
import javax.net.ssl.*

@Configuration
class RetrofitConfiguration(
    private val reportFilter: ReportInterceptor,
    private val loggingFilter: LoggingInterceptor,
    private val headerInterceptor: HeaderInterceptor,
    @param:Value("\${test-automation.integration.http.connection-time-out:30}") private val httpTimeout: Int
) {
    @Bean
    @Qualifier("testHttpClient")
    fun createRetrofitApiWithTMOInterceptors(): OkHttpClient {
        return httpClientWithInterceptors(
            Arrays.asList(
                headerInterceptor,
                loggingFilter,
                reportFilter
            )
        )
            .build()
    }

    private fun httpClientWithInterceptors(interceptors: List<Interceptor>): OkHttpClient.Builder {
        val builder = preBakedHttpClient
        interceptors.forEach(Consumer { interceptor: Interceptor? ->
            builder.addInterceptor(
                interceptor!!
            )
        })
        return builder
    }

    //I know these numbers are huge, but sometimes http calls like to take their sweet time responding with 10+ seconds of read being
    // not uncommon
    private val preBakedHttpClient: OkHttpClient.Builder
        private get() {
            val trustManager = trustManager
            return OkHttpClient.Builder()
                .sslSocketFactory(getSSLSocketFactory(trustManager), trustManager)
                .hostnameVerifier { hostname: String?, session: SSLSession? -> true } //I know these numbers are huge, but sometimes http calls like to take their sweet time responding with 10+ seconds of read being
                // not uncommon
                .readTimeout(Duration.ofSeconds(httpTimeout.toLong()))
                .connectTimeout(Duration.ofSeconds(httpTimeout.toLong()))
                .writeTimeout(Duration.ofSeconds(httpTimeout.toLong()))
                .callTimeout(Duration.ofSeconds(httpTimeout.toLong()))
        }

    @SneakyThrows
    private fun getSSLSocketFactory(trustManager: X509TrustManager): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        return sslContext.socketFactory
    }

    private val trustManager: X509TrustManager
        private get() = object : X509TrustManager {
            override fun checkClientTrusted(x509Certificates: Array<X509Certificate>, s: String) {}
            override fun checkServerTrusted(x509Certificates: Array<X509Certificate>, s: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOfNulls(0)
            }
        }

    private class FloatTypeAdapter : TypeAdapter<Float?>() {
        @Throws(IOException::class)
        override fun read(reader: JsonReader): Float? {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                return null
            }
            val stringValue = reader.nextString()
            return try {
                stringValue.toFloat()
            } catch (e: NumberFormatException) {
                null
            }
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: Float?) {
            if (value == null) {
                writer.nullValue()
                return
            }
            writer.value(value)
        }
    }
}