dependencies {
    kapt(libs.springboot.configuration.processor)

    api(project(":test-automation-core"))

    api(libs.testcontainers.core)
    api(libs.testcontainers.mockserver)
    api("org.mock-server:mockserver-client-java:5.15.0") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    api(libs.bundles.springboot)

    implementation(libs.bundles.jacoco)
    implementation(libs.commons.io)
}