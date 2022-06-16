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
package com.nortal.test.restassured

import com.fasterxml.jackson.databind.ObjectMapper
import com.nortal.test.core.services.TestableApplicationInfoProvider
import com.nortal.test.core.services.hooks.BeforeSuiteHook
import com.nortal.test.restassured.logs.ExclusionEnablingBasicClientConnectionManager
import io.restassured.RestAssured
import io.restassured.config.HttpClientConfig
import io.restassured.config.ObjectMapperConfig
import io.restassured.mapper.ObjectMapperType
import io.restassured.path.json.mapper.factory.Jackson2ObjectMapperFactory
import org.apache.http.impl.client.DefaultHttpClient
import org.springframework.stereotype.Component
import java.lang.reflect.Type

/**
 * This class holds all the pre run configuration for RestAssured.
 */
@Component
class RestAssuredPreparationHook(
    private val objectMapperProvider: RestAssuredConfiguration.ObjectMapperProvider,
    private val portProvider: TestableApplicationInfoProvider,
) : BeforeSuiteHook {

    /**
     * We configure rest assured to use ISO_LOCAL_DATE_TIME format when serializing LocalDateTime objects.
     * This is required because while backend pojos use LocalDateTime for their parameters,
     * the rest endpoints expect the dates to come in ISO format.
     */
    override fun beforeSuite() {
        val mapperConfig: ObjectMapperConfig = ObjectMapperConfig(ObjectMapperType.JACKSON_2)
            .jackson2ObjectMapperFactory(JacksonFactory(objectMapperProvider.getObjectMapper()))
        val httpClientConfig = HttpClientConfig()
            .httpClientFactory { DefaultHttpClient(ExclusionEnablingBasicClientConnectionManager()) }

        RestAssured.port = portProvider.getPort()
        RestAssured.config = RestAssured
            .config()
            .objectMapperConfig(mapperConfig)
            .httpClient(httpClientConfig)
    }

    private class JacksonFactory(private val objectMapper: ObjectMapper) : Jackson2ObjectMapperFactory {
        override fun create(cls: Type, charset: String): ObjectMapper {
            return objectMapper
        }
    }

}