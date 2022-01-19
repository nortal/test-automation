package com.nortal.test.report.allure

import com.nortal.test.TestRunner
import org.junit.platform.suite.api.SelectClasspathResource

@SelectClasspathResource("/behavior")
class AllureReportTest : TestRunner()