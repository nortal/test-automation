plugins {
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    kapt(libs.springboot.configuration.processor)

    implementation(project(":test-automation-core"))

    implementation(libs.allure.cucumber) {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "io.cucumber", module = "gherkin")
    }
    implementation(libs.allure.plugin.api) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation(libs.allure.commandline) {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "com.opencsv", module = "opencsv")
    }

    implementation(libs.opencsv)

    api(libs.commons.io)
}