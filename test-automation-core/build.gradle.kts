plugins {
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    kapt(libs.springboot.configuration.processor)

    api(libs.bundles.springboot)
    api(libs.bundles.cucumber)

    api(libs.jackson.module.kotlin)
    api(libs.jackson.datatype.jsr310)

    implementation(libs.swagger.request.validator.core) //TODO create story
    implementation(libs.cucumber.reporting)
    implementation(libs.org.eclipse.jgit)
}