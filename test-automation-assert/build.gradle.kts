description = "Assertion/validation helpers. Usually used with openFeign client."

dependencies {
    kapt(libs.springboot.configuration.processor)

    api(project(":test-automation-core"))

    api(libs.jsonpath)
}