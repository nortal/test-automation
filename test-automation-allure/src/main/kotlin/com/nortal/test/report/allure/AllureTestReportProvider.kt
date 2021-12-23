package com.nortal.test.report.allure

import com.nortal.test.core.cucumber.TestReportProvider
import io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm


class AllureTestReportProvider : TestReportProvider {

    override fun getCucumberPlugin(): String {
        return AllureCucumber7Jvm::class.java.name
    }


}