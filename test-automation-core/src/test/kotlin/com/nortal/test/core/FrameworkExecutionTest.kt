package com.nortal.test.core

import com.nortal.test.TestRunner
import org.junit.platform.suite.api.SelectClasspathResource

@SelectClasspathResource("/behavior")
class FrameworkExecutionTest : TestRunner()