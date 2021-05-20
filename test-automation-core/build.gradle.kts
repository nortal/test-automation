plugins {
    id("com.nortal.test.java-conventions")
    id("io.freefair.lombok")
}

configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
        exclude("ch.qos.logback", "logback-classic")
    }
}

dependencies {
    implementation(project(":test-automation-postman"))

    api(libs.bundles.springboot)
    api(libs.bundles.retrofit2)
    api(libs.bundles.cucumber)

    api(libs.jackson.module.kotlin)
    api(libs.jackson.datatype.jsr310)

    api(libs.guava)
    api(libs.commons.codec)

    implementation(libs.swagger.request.validator.core)
    implementation(libs.cucumber.reporting)
    implementation(libs.tika.core)
    implementation(libs.org.eclipse.jgit)

    implementation(kotlin("stdlib-jdk8"))

}

description = "test-automation-core"
