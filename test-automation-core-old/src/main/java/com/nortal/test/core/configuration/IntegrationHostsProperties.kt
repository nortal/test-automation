package com.nortal.test.core.configuration

import lombok.Getter
import lombok.Setter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.util.HashMap

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "test-automation.integration")
class IntegrationHostsProperties /*implements PostmanHostAware*/ {
    @Setter
    @Getter
    private val hosts: HashMap<String, String>? = null
}