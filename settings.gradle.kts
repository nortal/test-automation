enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "test-automation"

include(":test-automation-core")
include(":test-automation-assert")
include("test-automation-containers")
include("test-automation-jdbc")
include("test-automation-restassured")
include("test-automation-feign")
include("test-automation-allure")
include("test-automation-selenide")
include("test-automation-aws")


plugins {
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion apply false
    kotlin("plugin.spring") version kotlinVersion apply false
    kotlin("kapt") version kotlinVersion apply false
    id("pl.allegro.tech.build.axion-release") version "1.13.2" apply false
    id("io.gitlab.arturbosch.detekt").version("1.19.0") apply false
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.6.10")

            version("cucumber", "7.2.2")
            version("spring-boot", "2.6.2")
            version("spring-cloud", "3.1.0")
            version("jackson", "2.12.3")
            version("testcontainers", "1.16.2")
            version("rest-assured", "4.4.0")
            version("feign", "11.8")

            alias("kotlin-stdlib-jdk8").to("org.jetbrains.kotlin", "kotlin-stdlib-jdk8").versionRef("kotlin")
            alias("kotlin-reflect").to("org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")
            alias("cucumber-java").to("io.cucumber", "cucumber-java").versionRef("cucumber")
            alias("cucumber-junit-platform-engine").to("io.cucumber", "cucumber-junit-platform-engine").versionRef("cucumber")
            alias("cucumber-spring").to("io.cucumber", "cucumber-spring").versionRef("cucumber")

            alias("junit-platform-suite").to("org.junit.platform", "junit-platform-suite").version("1.8.2")

            //Spring Boot
            alias("springboot-starter_").to("org.springframework.boot", "spring-boot-starter").versionRef("spring-boot")
            alias("springboot-starter-web").to("org.springframework.boot", "spring-boot-starter-web").versionRef("spring-boot")
            alias("springboot-starter-jdbc").to("org.springframework.boot", "spring-boot-starter-jdbc").versionRef("spring-boot")
            alias("springboot-starter-test").to("org.springframework.boot", "spring-boot-starter-test").versionRef("spring-boot")
            alias("springboot-starter-log4j2").to("org.springframework.boot", "spring-boot-starter").versionRef("spring-boot")
            alias("springboot-starter-mail").to("org.springframework.boot", "spring-boot-starter-mail").versionRef("spring-boot")
            alias("springboot-configuration-processor").to("org.springframework.boot", "spring-boot-configuration-processor")
                .versionRef("spring-boot")
            //Spring Boot cloud
            alias("springcloud-openfeign").to("org.springframework.cloud", "spring-cloud-starter-openfeign").versionRef("spring-cloud")
            alias("springcloud-aws").to("org.springframework.cloud", "spring-cloud-starter-aws").version("2.2.6.RELEASE")

            // API clients: restassured
            alias("restassured").to("io.rest-assured", "rest-assured").versionRef("rest-assured")
            alias("restassured-jsonpath").to("io.rest-assured", "json-path").versionRef("rest-assured")
            //API clients: feign
            alias("openfeign-okhttp").to("io.github.openfeign", "feign-okhttp").versionRef("feign")
            alias("openfeign-jackson").to("io.github.openfeign", "feign-jackson").versionRef("feign")

            alias("guava").to("com.google.guava", "guava").version("30.1.1-jre")
            alias("commons-codec").to("commons-codec", "commons-codec").version("1.15")
            alias("commons-io").to("commons-io", "commons-io").version("2.11.0")

            alias("swagger_request_validator_core").to("com.atlassian.oai", "swagger-request-validator-core").version("2.18.0")
            alias("org.eclipse.jgit").to("org.eclipse.jgit", "org.eclipse.jgit").version("5.11.0.202103091610-r")

            alias("testcontainers-core").to("org.testcontainers", "testcontainers").versionRef("testcontainers")
            alias("testcontainers-postgresql").to("org.testcontainers", "postgresql").versionRef("testcontainers")

            alias("jackson-module-kotlin").to("com.fasterxml.jackson.module", "jackson-module-kotlin").versionRef("jackson")
            alias("jackson-datatype-jsr310").to("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310").versionRef("jackson")

            alias("postgresql").to("org.postgresql", "postgresql").version("42.2.21")

            alias("jacoco-core").to("org.jacoco", "org.jacoco.core").version("0.8.7")
            alias("jacoco-report").to("org.jacoco", "org.jacoco.report").version("0.8.7")

            //reporting
            alias("allure-cucumber").to("io.qameta.allure", "allure-cucumber7-jvm").version("2.17.2")
            alias("allure-plugin-api").to("io.qameta.allure", "allure-plugin-api").version("2.17.2")
            alias("allure-commandline").to("io.qameta.allure", "allure-commandline").version("2.17.2")

            alias("cucumber-reporting").to("net.masterthought", "cucumber-reporting").version("5.6.1")
            //UI testing
            alias("selenide").to("com.codeborne", "selenide").version("6.1.2")

            bundle(
                "cucumber", listOf(
                    "cucumber-java",
                    "cucumber-spring",
                    "cucumber-junit-platform-engine",
                    "junit-platform-suite"
                )
            )
            bundle(
                "springboot", listOf(
                    "springboot-starter_", "springboot-starter-web", "springboot-starter-test",
                     "springboot-starter-mail", "springboot-starter-jdbc"
                )
            )
            bundle("restassured", listOf("restassured", "restassured-jsonpath"))
            bundle("jacoco", listOf("jacoco-core", "jacoco-report"))
        }
    }
}
