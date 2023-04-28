
dependencies {
    kapt(libs.springboot.configuration.processor)

    implementation(project(":test-automation-core"))

    api(libs.reportportal.cucumber){
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "io.cucumber", module = "cucumber-gherkin")
    }
    api(libs.reportportal.logger)
    api(libs.reportportal.client)
}