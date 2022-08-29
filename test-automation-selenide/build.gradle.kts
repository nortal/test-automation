plugins {
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    kapt(libs.springboot.configuration.processor)

    api(project(":test-automation-core"))
    api(libs.selenide) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    api(libs.selenide.proxy){
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    api(libs.allure.selenide)
}
