package com.nortal.ams.test.integration.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.nortal.test.jdbc.configuration.TestJdbcConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import retrofit2.converter.jackson.JacksonConverterFactory

@Configuration
@ComponentScan("com.nortal.ams.test.integration.*")
@Import(TestJdbcConfiguration::class)
class AmsIntTestBaseConfiguration {

    @Bean
    fun jacksonConverterFactory(): JacksonConverterFactory {
        return JacksonConverterFactory.create(createObjectMapper())
    }

    private fun createObjectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerModule(KotlinModule())
            .registerModule(JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }
}