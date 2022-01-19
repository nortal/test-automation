plugins {
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    kapt(libs.springboot.configuration.processor)

    implementation(project(":test-automation-core"))

    implementation(libs.allure.cucumber)
    implementation(libs.allure.plugin.api)
    implementation(libs.allure.commandline)

    implementation(libs.commons.io)
}