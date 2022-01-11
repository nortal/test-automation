package com.nortal.test.core.report

import com.nortal.test.core.configuration.TestAutomationProperties
import org.springframework.stereotype.Component

@Component
open class ReportNameProvider(private val testAutomationProperties: TestAutomationProperties) {

    open fun getName(): String {
        return testAutomationProperties.reportName + "-" + System.currentTimeMillis()
    }
}