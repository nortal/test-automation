plugins {
    id("com.nortal.test.java-conventions")
}

dependencies {
    api(project(":test-automation-core"))
    api(project(":test-automation-containers"))

    api(libs.testcontainers.postgresql)
}

description = "ams-int-test-automation"
