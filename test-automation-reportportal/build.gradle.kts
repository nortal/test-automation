plugins {
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    kapt(libs.springboot.configuration.processor)

    implementation(project(":test-automation-core"))

    implementation(libs.reportportal.cucumber)
    implementation(libs.reportportal.logger)
}