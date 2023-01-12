# Containers module

[TestContainers](https://www.testcontainers.org/) based module that provides a capability to run tests against a Docker container.

## Setup Testable Container

1. Install [Docker](https://docs.docker.com/get-docker/). It is recommended to use Docker Desktop.
2. Add a dependency of this module in your project.
3. Set `test-automation.containers.testable-container.*` properties in `application-override.yml`.
   * All available properties are defined in `ContainerProperties.kt`
4. Create container setup class that extends `AbstractTestableContainerSetup`.
   * Implement abstract methods.
   * Override `build` method and specify Docker image.

## Setup Contextual Container (optional)

Contextual containers are started before the testable container.
For example, this can be used to run database container if application under test depends on a database.

1. Set `test-automation.containers.contextContainers.*` properties in `application-override.yml`.
   * Specify container name (configuration key) `[yourContainerName]`.
   * To enable the container, set `[yourContainerName].enabled` to true.
2. Create contextual container setup class that extends `AbstractAuxiliaryContainer`.
   * Implement abstract methods.
   * Specify configuration key in method `getConfigurationKey`.


When test are executing, the containers should start running.
When the tests are finished running, the Docker containers will be stopped.

To reuse containers between runs, set `reuse-between-runs` to true in `application-override.yml`.