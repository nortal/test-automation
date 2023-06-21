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
package com.nortal.test.feign

import com.nortal.test.feign.configuration.FeignProperties
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import java.util.function.Consumer
import javax.net.ssl.*

class OkHttpClientFactory(
        private val feignProperties: FeignProperties
) {

    fun withInterceptors(interceptors: List<Interceptor>): OkHttpClient.Builder {
        val builder = preBakedHttpClient

        interceptors.forEach(Consumer { interceptor: Interceptor ->
            builder.addInterceptor(interceptor)
        })

        return builder
    }

    private val preBakedHttpClient: OkHttpClient.Builder
        get() {
            val trustManager = trustManager
            return OkHttpClient.Builder()
                .sslSocketFactory(getSSLSocketFactory(trustManager), trustManager)
                .hostnameVerifier { _: String?, _: SSLSession? -> true }
                .readTimeout(Duration.ofSeconds(feignProperties.readTimeout.toLong()))
                .connectTimeout(Duration.ofSeconds(feignProperties.connectTimeout.toLong()))
                .writeTimeout(Duration.ofSeconds(feignProperties.writeTimeout.toLong()))
                .callTimeout(Duration.ofSeconds(feignProperties.callTimeout.toLong()))
                .retryOnConnectionFailure(feignProperties.retryOnConnectionFailure)
                .followRedirects(feignProperties.followRedirects)
                .followSslRedirects(feignProperties.followSslRedirects)
        }

    private fun getSSLSocketFactory(trustManager: X509TrustManager): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        return sslContext.socketFactory
    }

    private val trustManager: X509TrustManager
        get() = object : X509TrustManager {
            override fun checkClientTrusted(x509Certificates: Array<X509Certificate>, s: String) {
                //do nothing
            }

            override fun checkServerTrusted(x509Certificates: Array<X509Certificate>, s: String) {
                //do nothing
            }

            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                return arrayOfNulls(0)
            }
        }


}