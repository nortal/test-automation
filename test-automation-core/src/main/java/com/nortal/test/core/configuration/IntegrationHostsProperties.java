package com.nortal.test.core.configuration;

import java.util.HashMap;

import com.nortal.test.postman.PostmanHostAware;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

@Component
@EnableConfigurationProperties
 @ConfigurationProperties(prefix = "test-automation.integration")
public class IntegrationHostsProperties implements PostmanHostAware {

	@Setter
	@Getter
	private HashMap<String, String> hosts;

}
