
dependencies {
    kapt(libs.springboot.configuration.processor)

    api(libs.bundles.springboot)
    api(libs.bundles.cucumber)

    api(libs.jackson.module.kotlin)
    api(libs.jackson.datatype.jsr310)

    api(libs.commons.lang3)
    api(libs.commons.io)

    implementation(libs.snakeyaml)
    implementation(libs.guava)
}