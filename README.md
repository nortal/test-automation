# Test Automation Framework

This is a modular testing framework which  can be used for **API** or **UI** testing.

**Documentation is still work in progress.**

# Features

## General
* Based on the popular Cucumber / Gherkin standard.
* Rest API testing.
* UI testing through Selenide.
* Integrated Test Containers support which allows you starting testable container and any other dependency (database, redis, elastic, etc.)
* Capable of running as a standalone test suite (for example E2E tests) and as part of application build (Integration/System tests)
* Multi-Threaded Execution which greatly speeds up test suite execution.
* Parallel execution can be grouped into isolated groups. Detailed feature description can be found here

## Reporting
* Advanced reporting capabilities that includes but is not limited to:
  * Executed request cUrls under each step/feature.
  * API responses.
  * Visualized validations inside a report.
* Report uploader to S3 bucket.

## Development and maintenance
* Quick and easy multi-dev configuration through YAML files.
* Easily extensible through modules and spring beans.

# Key Technologies

* Java 8+ (limited for better compatability across different projects.)
* Kotlin 1.7+ (It's a go-to language for framework modules, but java projects are fully supported.)
* Spring Boot 2.7+
* Cucumber 7
* JUnit 5