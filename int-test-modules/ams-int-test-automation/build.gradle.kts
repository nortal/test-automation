
dependencies {
    api(project(":test-automation-core"))
    api(project(":test-automation-containers"))
//    api(project(":test-automation-postman"))
    api(project(":test-automation-jdbc"))

    api(libs.testcontainers.postgresql)
    api(libs.postgresql)
}
