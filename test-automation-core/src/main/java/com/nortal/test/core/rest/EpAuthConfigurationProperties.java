package com.nortal.test.core.rest;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "integration.rest.ep")
public class EpAuthConfigurationProperties {

	/**
	 * basic auth token for integration/cm api
	 *  i.e. Basic 8136ae581bd66880
	 */
	private String basicAuth;
}
