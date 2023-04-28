
dependencies {
    kapt(libs.springboot.configuration.processor)

    api(project(":test-automation-core"))

    api(libs.springcloud.openfeign)
    api(libs.openfeign.core)
    api(libs.openfeign.okhttp)
    api(libs.openfeign.jackson)
}