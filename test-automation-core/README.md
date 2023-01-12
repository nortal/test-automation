# Test Automation Core

This module contains main functionality that ties all other modules.

Core module must be added to your project before you can use any other module.

## Setup

1. Add a dependency of this module in your project.
2. Set configuration in `application-override.yml`.
   * Specify `test-automation.report-name`.
   * Set Cucumber properties `test-automation.cucumber` and glue package location `test-automation.cucumber.glue-append`.

This module includes Spring Boot, Cucumber, Jackson and other essential elements for Test Automation Framework