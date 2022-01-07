package com.nortal.test.report.allure.configuration

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(
    AllureReportProperties::class,
    AllureServeReportProperties::class
)
@Configuration
open class AllureConfiguration