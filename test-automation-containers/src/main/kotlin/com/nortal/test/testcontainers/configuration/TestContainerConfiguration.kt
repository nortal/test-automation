package com.nortal.test.testcontainers.configuration

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(
    TestContainerProperties::class,
    TestableContainerProperties::class,
    TestableContainerJacocoProperties::class
)
class TestContainerConfiguration