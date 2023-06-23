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
package com.nortal.test.feign.configuration

import com.nortal.test.feign.DefaultTargeter
import com.nortal.test.feign.OkHttpClientFactory
import com.nortal.test.feign.interceptor.FeignClientInterceptor
import feign.Client
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
import java.util.*
import javax.net.ssl.*


/**
 * Feign bean configuration. Loosely follows [FeignAutoConfiguration].
 */
@Configuration
@EnableConfigurationProperties(
    FeignProperties::class,
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
        return FeignContext()
    }

    @Bean
    open fun feignClient(
        feignClientInterceptors: List<FeignClientInterceptor>,
        feignProperties: FeignProperties
    ): Client {
        val client = OkHttpClientFactory(feignProperties)
            .withInterceptors(feignClientInterceptors.sortedBy { it.order })
            .build()

        return feign.okhttp.OkHttpClient(client)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun feignTargeter(): Targeter? {
        return DefaultTargeter()
    }


}