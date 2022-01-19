package com.nortal.test.testcontainers.configuration

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(
    ContainerProperties::class,
    TestableContainerProperties::class,
    TestableContainerJacocoProperties::class
)
open class TestContainerConfiguration