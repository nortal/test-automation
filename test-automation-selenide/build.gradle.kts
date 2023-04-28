
dependencies {
    kapt(libs.springboot.configuration.processor)

    api(project(":test-automation-core"))
    api(libs.selenide) {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "io.netty", module = "*")
    }
    api(libs.selenide.proxy) {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "io.netty", module = "*")
        exclude(group = "com.github.valfirst.browserup-proxy", module = "*")
    }
    api(libs.allure.selenide)
    api(libs.netty)
    api(libs.browserup.proxy) {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "io.netty", module = "*")
        exclude(group = "org.seleniumhq.selenium", module = "*")
    }
}
