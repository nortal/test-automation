plugins {
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    kapt(libs.springboot.configuration.processor)

    api(project(":test-automation-core"))

    api(libs.springcloud.openfeign)

    // https://mvnrepository.com/artifact/io.github.openfeign/feign-okhttp
    implementation("io.github.openfeign:feign-okhttp:11.7")
    // https://mvnrepository.com/artifact/io.github.openfeign/feign-jackson
    implementation("io.github.openfeign:feign-jackson:11.7")


}
