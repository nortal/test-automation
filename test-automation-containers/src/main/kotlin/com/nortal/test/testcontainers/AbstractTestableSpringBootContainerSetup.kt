package com.nortal.test.testcontainers

abstract class AbstractTestableSpringBootContainerSetup : AbstractTestableContainerSetup() {

    override fun getTargetContainerEnvConfig(): Map<String, String> {
        return mapOf(
            "spring.profiles.active" to testableContainerProperties.springProfilesToActivate
        )
    }
}