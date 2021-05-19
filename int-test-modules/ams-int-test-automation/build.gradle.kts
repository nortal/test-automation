import java.util.Properties;

plugins {
    `java-library`
    `maven-publish`
    id("com.nortal.test.java-conventions")
}

dependencies {
    api(project(":test-automation-core"))
    api(project(":test-automation-containers"))

    api(libs.testcontainers.postgresql)
}

description = "ams-int-test-automation"

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
        create<MavenPublication>("ams-int-test-automation") {
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
