package com.nortal.test.restassured

import com.fasterxml.jackson.databind.ObjectMapper
import com.nortal.test.core.services.TestableApplicationPortProvider
import com.nortal.test.core.services.hooks.BeforeSuiteHook
import com.nortal.test.restassured.logs.ExclusionEnablingBasicClientConnectionManager
import io.restassured.RestAssured
import io.restassured.config.HttpClientConfig
import io.restassured.config.ObjectMapperConfig
import io.restassured.mapper.ObjectMapperType
import io.restassured.path.json.mapper.factory.Jackson2ObjectMapperFactory
import org.apache.http.impl.client.DefaultHttpClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.lang.reflect.Type

/**
 * This class holds all the pre run configuration for RestAssured.
 */
@Component
class RestAssuredPreparationHook(
    private val objectMapperProvider: RestAssuredConfiguration.ObjectMapperProvider,
    private val portProvider: TestableApplicationPortProvider,
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