
dependencies {
    kapt(libs.springboot.configuration.processor)

    api(project(":test-automation-core"))

    api(libs.testcontainers.core)
    api(libs.testcontainers.mockserver)
    api("org.mock-server:mockserver-client-java:5.15.0")
    api(libs.bundles.springboot)

    implementation(libs.bundles.jacoco)
    implementation(libs.commons.io)
}