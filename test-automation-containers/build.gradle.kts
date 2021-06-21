plugins {
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    kapt(libs.springboot.configuration.processor)

    api(project(":test-automation-core"))
    api(libs.testcontainers.core)
    api("org.testcontainers:mockserver:1.15.3")
    api("org.mock-server:mockserver-client-java:5.11.2")
    api(libs.bundles.springboot)

    implementation(libs.bundles.jacoco)
    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("commons-io:commons-io:2.5")
    implementation(libs.bundles.retrofit2)
}