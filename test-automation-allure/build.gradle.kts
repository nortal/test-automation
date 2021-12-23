plugins {
    id("org.jetbrains.kotlin.kapt")
    id("io.qameta.allure")
}

dependencies {
    kapt(libs.springboot.configuration.processor)

    implementation(project(":test-automation-core"))
    implementation(libs.allure.cucumber)
    implementation("io.qameta.allure:allure-commandline:2.17.2")
    implementation(libs.commons.io)
}

allure {
    adapter {
        autoconfigure.set(true)
    }

}