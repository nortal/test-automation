package com.nortal.test.core.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Main configuration class for spring context.
 */
@ComponentScan({"com.nortal.test","com.nortal.test.core.configuration"})
@EnableConfigurationProperties
@Configuration
public class TestConfiguration {

	/*
	@Bean("triggerJMSQueue")
	public TriggerJMSQueue triggerJMSQueue(
		@Value("${ep.activemq.url}") final String url,
		@Value("${ep.activemq.username}") final String username,
		@Value("${ep.activemq.password}") final String password
	) {
		return new TriggerJMSQueue(url, username, password);
	}*/

}
