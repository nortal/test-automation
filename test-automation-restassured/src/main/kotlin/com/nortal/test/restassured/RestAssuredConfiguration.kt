package com.nortal.test.restassured

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.context.annotation.Configuration

@Configuration
open class RestAssuredConfiguration {


    open fun defaultObjectMapperProvider(): ObjectMapperProvider {
        return ObjectMapperProvider(
            ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        )
    }


    data class ObjectMapperProvider(private val objectMapper: ObjectMapper) {
        fun getObjectMapper(): ObjectMapper {
            return objectMapper
        }
    }

}