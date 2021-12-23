package com.nortal.test.feign.configuration

import com.nortal.test.feign.DefaultTargeter
import com.nortal.test.feign.OkHttpClientFactory
import com.nortal.test.feign.interceptor.LoggingInterceptor
import com.nortal.test.feign.interceptor.ReportInterceptor
import com.nortal.test.feign.interceptor.TargetHostUpdateInterceptor
import feign.Client
import feign.RequestInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.openfeign.FeignClientProperties
import org.springframework.cloud.openfeign.FeignContext
import org.springframework.cloud.openfeign.Targeter
import org.springframework.cloud.openfeign.support.FeignEncoderProperties
import org.springframework.cloud.openfeign.support.FeignHttpClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import java.io.IOException
import java.lang.NumberFormatException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import java.util.*
import javax.net.ssl.*


/**
 * Feign bean configuration. Loosely follows [FeignAutoConfiguration].
 */
@Configuration
@EnableConfigurationProperties(
    FeignClientProperties::class,
    FeignHttpClientProperties::class,
    FeignEncoderProperties::class
)
open class FeignConfiguration {

    @Bean
    open fun httpMessageConverters(): HttpMessageConverters {
        return HttpMessageConverters(
            MappingJackson2HttpMessageConverter()
        )
    }

    @Bean
    open fun feignContext(): FeignContext {
        val context = FeignContext()
//        context.setConfigurations()
//        context
//        context.setConfigurations(this.configurations)
        return context
    }

    @Bean
    open fun feignClient(
        loggingInterceptor: LoggingInterceptor,
        reportInterceptor: ReportInterceptor,
        targetHostUpdateInterceptor: TargetHostUpdateInterceptor
    ): Client {
        val client = OkHttpClientFactory()
            .withInterceptors(listOf(loggingInterceptor, reportInterceptor, targetHostUpdateInterceptor))
            .build()

        return feign.okhttp.OkHttpClient(client)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun feignTargeter(): Targeter? {
        return DefaultTargeter()
    }


}