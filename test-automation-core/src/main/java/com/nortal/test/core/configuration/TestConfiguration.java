package com.nortal.test.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Main configuration class for spring context.
 */
@ComponentScan({"com.nortal.test", "com.nortal.test.core.configuration"})
@EnableConfigurationProperties
@Configuration
public class TestConfiguration {

	@Bean("apiObjectMapper")
	public ObjectMapper apiObjectMapper() {
		return new ObjectMapper()
				.registerModule(new KotlinModule())
				.registerModule(new JavaTimeModule())
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}
}
