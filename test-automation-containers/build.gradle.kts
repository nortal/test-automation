plugins {
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    kapt(libs.springboot.configuration.processor)

    api(project(":test-automation-core"))

    api(libs.testcontainers.core)
    api("org.testcontainers:mockserver:1.17.4")
    api("org.mock-server:mockserver-client-java:5.14.0")
    api(libs.bundles.springboot)

    implementation(libs.bundles.jacoco)
    implementation(libs.commons.io)
}