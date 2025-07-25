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
include("test-automation-reportportal")

include(":demos:demo-ui-test")
include(":demos:demo-testcontainers-api-test")

plugins {
    val kotlinVersion = "1.8.20"
    kotlin("jvm") version kotlinVersion apply false
    kotlin("plugin.spring") version kotlinVersion apply false
    kotlin("kapt") version kotlinVersion apply false
    id("pl.allegro.tech.build.axion-release") version "1.14.4" apply false
    id("io.gitlab.arturbosch.detekt").version("1.22.0") apply false
    id("com.github.hierynomus.license").version("0.16.1") apply false
    id("org.jetbrains.dokka") version "1.9.10" apply false
    id("io.freefair.lombok") version "8.4" apply false
    id("org.springframework.boot") version "3.1.5" apply false
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.9.25")

            version("cucumber", "7.18.1")
            version("spring-boot", "3.4.6")
            version("spring-cloud", "4.2.0")
            version("jackson", "2.19.0")
            version("testcontainers", "1.21.1")
            version("rest-assured", "5.5.5")
            version("feign", "13.6")
            version("jacoco", "0.8.13")
            version("allure", "2.28.0")
            version("reportportal", "5.1.2")
            version("selenide", "7.9.3")
            version("netty", "4.1.122.Final")
            version("browserup-proxy", "3.2.0")

            library("kotlin-stdlib-jdk8", "org.jetbrains.kotlin", "kotlin-stdlib-jdk8").versionRef("kotlin")
            library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")
            library("cucumber-java", "io.cucumber", "cucumber-java").versionRef("cucumber")
            library("cucumber-junit-platform-engine", "io.cucumber", "cucumber-junit-platform-engine").versionRef("cucumber")
            library("cucumber-spring", "io.cucumber", "cucumber-spring").versionRef("cucumber")

            library("junit-platform-suite", "org.junit.platform", "junit-platform-suite").version("1.10.3")

            //Spring Boot
            library("springboot-starter_", "org.springframework.boot", "spring-boot-starter").versionRef("spring-boot")
            library("springboot-starter-web", "org.springframework.boot", "spring-boot-starter-web").versionRef("spring-boot")
            library("springboot-starter-jdbc", "org.springframework.boot", "spring-boot-starter-jdbc").versionRef("spring-boot")
            library("springboot-starter-test", "org.springframework.boot", "spring-boot-starter-test").versionRef("spring-boot")
            library("springboot-starter-log4j2", "org.springframework.boot", "spring-boot-starter").versionRef("spring-boot")
            library("springboot-starter-mail", "org.springframework.boot", "spring-boot-starter-mail").versionRef("spring-boot")
            library("springboot-configuration-processor", "org.springframework.boot", "spring-boot-configuration-processor")
                .versionRef("spring-boot")
            //Spring Boot cloud
            library("springcloud-openfeign", "org.springframework.cloud", "spring-cloud-starter-openfeign").versionRef("spring-cloud")
            library("springcloud-aws", "org.springframework.cloud", "spring-cloud-starter-aws").version("2.2.6.RELEASE")

            // API clients: restassured
            library("restassured", "io.rest-assured", "rest-assured").versionRef("rest-assured")
            library("restassured-jsonpath", "io.rest-assured", "json-path").versionRef("rest-assured")
            //API clients: feign
            library("openfeign-core", "io.github.openfeign", "feign-core").versionRef("feign")
            library("openfeign-okhttp", "io.github.openfeign", "feign-okhttp").versionRef("feign")
            library("openfeign-jackson", "io.github.openfeign", "feign-jackson").versionRef("feign")

            library("guava", "com.google.guava", "guava").version("33.4.0-jre")

            library("opencsv", "com.opencsv", "opencsv").version("5.10")

            library("commons-lang3", "org.apache.commons", "commons-lang3").version("3.17.0")
//            library("commons-codec", "commons-codec", "commons-codec").version("1.17.1")
            library("commons-io", "commons-io", "commons-io").version("2.18.0")

            library("swagger_request_validator_core", "com.atlassian.oai", "swagger-request-validator-core").version("2.18.0")
            library("org.eclipse.jgit", "org.eclipse.jgit", "org.eclipse.jgit").version("6.2.0.202206071550-r")
            library("jsonpath", "com.jayway.jsonpath", "json-path").version("2.9.0")

            library("testcontainers-core", "org.testcontainers", "testcontainers").versionRef("testcontainers")
            library("testcontainers-mockserver", "org.testcontainers", "mockserver").versionRef("testcontainers")
            library("testcontainers-postgresql", "org.testcontainers", "postgresql").versionRef("testcontainers")

            library("jackson-databind", "com.fasterxml.jackson.core", "jackson-databind").versionRef("jackson")
            library("jackson-module-kotlin", "com.fasterxml.jackson.module", "jackson-module-kotlin").versionRef("jackson")
            library("jackson-datatype-jsr310", "com.fasterxml.jackson.datatype", "jackson-datatype-jsr310").versionRef("jackson")

            library("jacoco-core", "org.jacoco", "org.jacoco.core").versionRef("jacoco")
            library("jacoco-report", "org.jacoco", "org.jacoco.report").versionRef("jacoco")

            //reporting
            library("allure-cucumber", "io.qameta.allure", "allure-cucumber7-jvm").versionRef("allure")
            library("allure-selenide", "io.qameta.allure", "allure-selenide").versionRef("allure")
            library("allure-plugin-api", "io.qameta.allure", "allure-plugin-api").versionRef("allure")
            library("allure-commandline", "io.qameta.allure", "allure-commandline").versionRef("allure")

            library("reportportal-client", "com.epam.reportportal", "client-java").version("5.1.11")
            library("reportportal-cucumber", "com.epam.reportportal", "agent-java-cucumber6").versionRef("reportportal")
            library("reportportal-logger", "com.epam.reportportal", "logger-java-logback").versionRef("reportportal")

            //UI testing
            library("selenide", "com.codeborne", "selenide").versionRef("selenide")
            library("selenide-proxy", "com.codeborne", "selenide-proxy").versionRef("selenide")
            library("netty", "io.netty", "netty-all").versionRef("netty")
            library("browserup-proxy", "com.github.valfirst.browserup-proxy", "browserup-proxy-core").versionRef("browserup-proxy")

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
