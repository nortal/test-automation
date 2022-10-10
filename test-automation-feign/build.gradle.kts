plugins {
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    kapt(libs.springboot.configuration.processor)

    api(project(":test-automation-core"))

    api(libs.springcloud.openfeign)
    api(libs.openfeign.okhttp)
    implementation(libs.openfeign.jackson)
}