package com.nortal.test.core.configuration

import lombok.Data
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@Data
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "stack")
class StackProperties {
    private val name: String? = null
}