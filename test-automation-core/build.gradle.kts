plugins {
    id("org.jetbrains.kotlin.kapt")
}

configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
        exclude("ch.qos.logback", "logback-classic")
    }
}

dependencies {
    kapt(libs.springboot.configuration.processor)

    api(libs.bundles.springboot)
    api(libs.bundles.cucumber)

    api(libs.jackson.module.kotlin)
    api(libs.jackson.datatype.jsr310)

    implementation(libs.swagger.request.validator.core)
    implementation(libs.cucumber.reporting)
    implementation(libs.org.eclipse.jgit)
}
