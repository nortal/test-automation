package com.nortal.test.report.allure.hook

import com.nortal.test.core.services.hooks.BeforeSuiteHook
import io.qameta.allure.util.PropertiesUtils
import org.apache.commons.io.FileUtils
import org.springframework.stereotype.Component
import java.nio.file.Paths

@Component
class AllureReportPrepHook : BeforeSuiteHook {

    override fun beforeSuiteOrder(): Int {
        return Integer.MIN_VALUE
    }

    override fun beforeSuite() {
        val properties = PropertiesUtils.loadAllureProperties()
        val path = properties.getProperty("allure.results.directory", "allure-results")


        val resultDir = Paths.get(path).toFile()
        if (resultDir.isDirectory && resultDir.exists()) {
            FileUtils.deleteDirectory(resultDir)
        }

    }
}