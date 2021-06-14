package com.nortal.test.jdbc

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("test-automation.integration.jdbc")
data class JdbcDataSourceProperties(
    val driverClassName: String,
    val url: String,
    val username: String,
    val password: String
)