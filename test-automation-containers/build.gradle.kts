import java.util.Properties;

plugins {
    `java-library`
    `maven-publish`
    id("com.nortal.test.java-conventions")
}

dependencies {
    api(libs.testcontainers.core)
    api( "org.testcontainers:mockserver:1.15.3")
    api("org.mock-server:mockserver-client-java:5.11.2")
    api(libs.bundles.springboot)

    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("commons-io:commons-io:2.5")
    implementation(libs.bundles.retrofit2)
}

description = "test-automation-containers"

val props = Properties()
rootProject.file("gradle-local.properties").takeIf { it.exists() }?.inputStream()?.use { props.load(it) }
val nexusUrl: String = System.getenv("GTCT_AMS_NEXUS_URL") ?: props.getProperty("nexusUrl")

repositories {
    mavenCentral()
    maven("https://$nexusUrl/repository/ams-maven/") {
        credentials {
            username = System.getenv("GTCT_AMS_NEXUS_USERNAME") ?: props.getProperty("nexusUsername")
            password = System.getenv("GTCT_AMS_NEXUS_PASSWORD") ?: props.getProperty("nexusPassword")
        }
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

publishing {
    publications {
        create<MavenPublication>("test-automation-containers") {
            from(components["java"])
            artifact(sourcesJar)
        }
    }

    repositories {
        val snapshotsRepoUrl = "https://$nexusUrl/repository/ams-maven-snapshots/"
        val releasesRepoUrl = "https://$nexusUrl/repository/ams-maven-releases/"

        maven(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl) {
            credentials {
                username = System.getenv("GTCT_AMS_NEXUS_USERNAME") ?: props.getProperty("nexusUsername")
                password = System.getenv("GTCT_AMS_NEXUS_PASSWORD") ?: props.getProperty("nexusPassword")
            }
        }
    }
}

