plugins {
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    kapt(libs.springboot.configuration.processor)

    api(project(":test-automation-core"))

    api(libs.springcloud.openfeign)
    implementation(libs.openfeign.jackson)
    implementation(libs.openfeign.okhttp)
}