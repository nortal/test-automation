
configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
        exclude("ch.qos.logback", "logback-classic")
    }
}

dependencies {
    api("org.springframework.boot:spring-boot-starter:2.4.5")
    api("org.springframework.boot:spring-boot-starter-mail:2.4.5")
    api("org.springframework.boot:spring-boot-starter-web:2.4.5")
    api("org.springframework.boot:spring-boot-starter-log4j2:2.4.5")
    api("org.springframework.cloud:spring-cloud-starter-netflix-zuul:2.2.8.RELEASE")
    implementation(kotlin("stdlib-jdk8"))
}