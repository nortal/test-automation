import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    `java-library`
    `maven-publish`
    id("pl.allegro.tech.build.axion-release")
    id("io.gitlab.arturbosch.detekt")
}

scmVersion {
    localOnly = true
    ignoreUncommittedChanges = false

    tag(closureOf<pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig> {
        prefix = "v"
        versionSeparator = ""
    })
    repository(closureOf<pl.allegro.tech.build.axion.release.domain.RepositoryConfig> {
        pushTagsOnly = true
    })
    checks(closureOf<pl.allegro.tech.build.axion.release.domain.ChecksConfig> {
        aheadOfRemote = false
    })
}

version = scmVersion.version

val targetJavaVersion = JavaVersion.VERSION_1_8

class MissingRequiredPropertyException(envVarName: String, propName: String) : GradleException(
    "No '$envVarName' environment variable nor '$propName' in 'gradle-local.properties' are configured"
)

fun Properties.getRequiredProperty(envVarName: String, propName: String) =
    System.getenv(envVarName).takeUnless { it.isNullOrBlank() }
        ?: this.getProperty(propName).takeUnless { it.isNullOrBlank() }
        ?: throw MissingRequiredPropertyException(envVarName, propName)

val props = Properties()
rootProject.file("gradle-local.properties").takeIf { it.exists() }?.inputStream()?.use { props.load(it) }

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        /*maven(props.getRequiredProperty("MAVEN_MIRROR_REPOSITORY_URL", "mirrorRepositoryUrl")) {
            credentials {
                username = props.getRequiredProperty("MAVEN_MIRROR_REPOSITORY_USERNAME", "mirrorRepositoryUsername")
                password = props.getRequiredProperty("MAVEN_MIRROR_REPOSITORY_PASSWORD", "mirrorRepositoryPassword")
            }
        }
        maven {
            url = uri(props.getRequiredProperty("MAVEN_RELEASES_REPOSITORY_URL", "releasesRepoUrl"))
            authentication { create<HttpHeaderAuthentication>("") }
            credentials(HttpHeaderCredentials::class) {
                name = System.getenv("CI_JOB_TOKEN")?.let { "Job-Token" } ?: "Private-Token"
                value = props.getRequiredProperty("CI_JOB_TOKEN", "personalAccessToken")
            }
        }
        maven {
            url = uri(props.getRequiredProperty("MAVEN_SNAPSHOTS_REPOSITORY_URL", "snapshotsRepoUrl"))
            authentication { create<HttpHeaderAuthentication>("") }
            credentials(HttpHeaderCredentials::class) {
                name = System.getenv("CI_JOB_TOKEN")?.let { "Job-Token" } ?: "Private-Token"
                value = props.getRequiredProperty("CI_JOB_TOKEN", "personalAccessToken")
            }
        }*/
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
        plugin("io.gitlab.arturbosch.detekt")
    }

    group = "com.nortal.test"
    version = rootProject.version

    configure<JavaPluginExtension> {
        sourceCompatibility = targetJavaVersion
        targetCompatibility = targetJavaVersion
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
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-Xjvm-default=all",
                "-Xemit-jvm-type-annotations"
            )
            jvmTarget = targetJavaVersion.toString()
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        testLogging {
            events(PASSED, SKIPPED, FAILED, STANDARD_OUT, STANDARD_ERROR)
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showCauses = true
            showExceptions = true
            showStackTraces = true
            showStandardStreams = true
        }
    }

    val props = Properties()
    rootProject.file("gradle-local.properties").takeIf { it.exists() }?.inputStream()?.use { props.load(it) }

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
            maven {
                url = if (version.toString().endsWith("SNAPSHOT")) {
                    uri(props.getRequiredProperty("MAVEN_SNAPSHOTS_REPOSITORY_URL", "snapshotsRepoUrl"))
                } else {
                    uri(props.getRequiredProperty("MAVEN_RELEASES_REPOSITORY_URL", "releasesRepoUrl"))
                }
                authentication { create<HttpHeaderAuthentication>("") }
                credentials(HttpHeaderCredentials::class) {
                    name = System.getenv("CI_JOB_TOKEN")?.let { "Job-Token" } ?: "Private-Token"
                    value = props.getRequiredProperty("CI_JOB_TOKEN", "personalAccessToken")
                }
            }
        }
    }

    detekt {
        config = files("${project.rootDir}/detekt-config.yml")
    }
}