plugins {
    id("com.nortal.test.java-conventions")
}

dependencies {
    api(libs.testcontainers)
    api(libs.bundles.springboot)
    api("org.apache.commons:commons-lang3:3.9")
    api("commons-io:commons-io:2.5")
}

description = "test-automation-containers"
