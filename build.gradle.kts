import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    `java-library`
    `maven-publish`
    id("pl.allegro.tech.build.axion-release")
}

scmVersion {
    localOnly = true
    tag(closureOf<pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig> {
        prefix = "v"
        versionSeparator = ""
    })
    repository(closureOf<pl.allegro.tech.build.axion.release.domain.RepositoryConfig> {
        pushTagsOnly = true
    })
    checks(closureOf<pl.allegro.tech.build.axion.release.domain.ChecksConfig > {
        aheadOfRemote = false
    })

}

version = scmVersion.version

allprojects {
    val props = Properties()
    rootProject.file("gradle-local.properties").takeIf { it.exists() }?.inputStream()?.use { props.load(it) }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    }

}

subprojects {
    apply {
        plugin("java-library")
        plugin("maven-publish")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("io.freefair.lombok")
    }

    group = "com.nortal.test"
    version = rootProject.version

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<Jar> {
        archiveBaseName.set(project.name)
    }

    val implementation by configurations
    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events(PASSED, SKIPPED, FAILED)
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = JavaVersion.VERSION_11.majorVersion
        }
    }

    val props = Properties()
    rootProject.file("gradle-local.properties").takeIf { it.exists() }?.inputStream()?.use { props.load(it) }
    val nexusUrl: String = System.getenv("GTCT_AMS_NEXUS_URL") ?: props.getProperty("nexusUrl")

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://$nexusUrl/repository/ams-maven/") {
            credentials {
                username = System.getenv("GTCT_AMS_NEXUS_USERNAME") ?: props.getProperty("nexusUsername")
                password = System.getenv("GTCT_AMS_NEXUS_PASSWORD") ?: props.getProperty("nexusPassword")
            }
        }
    }

    val sourcesJar by tasks.creating(org.gradle.api.tasks.bundling.Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.getByName("main").allSource)
    }

    publishing {
        publications {
            create<MavenPublication>(project.name) {
                groupId = "${project.group}"
                artifactId = project.name
                version = version
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

}

tasks.withType<Wrapper> {
    gradleVersion = libs.versions.gradle.get()
    distributionType = Wrapper.DistributionType.BIN
}

