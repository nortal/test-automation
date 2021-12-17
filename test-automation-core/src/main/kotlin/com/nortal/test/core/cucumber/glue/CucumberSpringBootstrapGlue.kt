package com.nortal.test.core.cucumber.glue

import com.nortal.test.core.configuration.TestConfiguration
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Bootstrap Spring framework.
 */
@CucumberContextConfiguration
@ActiveProfiles(value = ["core", "base", "override"])
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