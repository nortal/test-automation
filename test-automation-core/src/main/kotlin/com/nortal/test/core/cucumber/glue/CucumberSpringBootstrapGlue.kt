/**
 * Copyright (c) 2022 Nortal AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.nortal.test.core.cucumber.glue

import com.nortal.test.core.configuration.TestAutomationConstants
import com.nortal.test.core.configuration.TestConfiguration
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Bootstrap Spring framework.
 */
@CucumberContextConfiguration
@ActiveProfiles(
    value = [
        TestAutomationConstants.SPRING_PROFILE_CORE,
        TestAutomationConstants.SPRING_PROFILE_BASE,
        TestAutomationConstants.SPRING_PROFILE_OVERRIDE
    ]
)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = [TestConfiguration::class],

    /* TODO:LTBENCH-140 this enables a custom property filename, but the issue is that IntelliJ does not pick it up automatically.
         Leaving it disabled until we have better approach.
         properties = [
         "spring.config.name=test-framework",
         "spring.cloud.config.enabled=false"
     ]*/
)
class CucumberSpringBootstrapGlue