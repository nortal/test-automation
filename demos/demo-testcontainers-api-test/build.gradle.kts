import org.gradle.jvm.tasks.Jar
import org.springframework.boot.gradle.tasks.bundling.BootJar

description = "demo-testcontainer-api-test"

plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(libs.springboot.starter.web)

    testImplementation(project(":test-automation-core"))
    testImplementation(project(":test-automation-allure"))
    testImplementation(project(":test-automation-assert"))
    testImplementation(project(":test-automation-feign"))
    testImplementation(project(":test-automation-containers"))
}

tasks.withType<Jar> {
    enabled = false
}

tasks.withType<BootJar> {
    enabled = true
}