import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    `java-library`
    `maven-publish`
    signing
    id("pl.allegro.tech.build.axion-release")
    id("io.gitlab.arturbosch.detekt")
    id("com.github.hierynomus.license")
}

scmVersion {
    localOnly.set(true)
    ignoreUncommittedChanges.set(false)

    tag {
        prefix.set("v")
        versionSeparator.set("")
    }
    repository {
        pushTagsOnly.set(true)
    }
    checks {
        aheadOfRemote.set(false)
    }
    nextVersion {
        suffix.set("beta")
        separator.set("-")
    }
}

version = scmVersion.version

 val targetJavaVersion = JavaVersion.VERSION_11

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
    apply {
        plugin("java-library")
    }

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
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("io.gitlab.arturbosch.detekt")
        plugin("com.github.hierynomus.license")
    }

    group = "com.nortal.test"
    version = rootProject.version

    configure<JavaPluginExtension> {
        sourceCompatibility = targetJavaVersion
        targetCompatibility = targetJavaVersion
    }

    val implementation by configurations
    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
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

        if (!project.hasProperty("intTests")) {
            exclude("**/**IntTest**")
        }

        testLogging {
            events(PASSED, SKIPPED, FAILED, STANDARD_OUT, STANDARD_ERROR)
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showCauses = true
            showExceptions = true
            showStackTraces = true
            showStandardStreams = true
        }
    }

    detekt {
        config = files("${project.rootDir}/detekt-config.yml")
    }

    license {
        header = file("${project.rootDir}/LICENSE_HEADER")
        isStrictCheck = true
        isSkipExistingHeaders = true

        excludes(
            listOf(
                "**/*.jar",
                "**/*.xml",
                "**/*.yml",
                "**/*.yaml",
                "**/*.properties",
                "**/*.html",
                "**/*.css",
                "**/*.feature",
                "**/*.txt",
            )
        )
        ext["year"] = Calendar.getInstance().get(Calendar.YEAR).toString()
    }
}

configure(subprojects.filter { it.name !in setOf("demos", "demo-ui-test") }) {
    apply {
        plugin("maven-publish")
        plugin("org.jetbrains.kotlin.kapt")
        plugin("org.jetbrains.dokka")
        plugin("signing")
    }

    tasks.withType<Jar> {
        archiveBaseName.set(project.name)
    }

    val props = Properties()
    rootProject.file("gradle-local.properties").takeIf { it.exists() }?.inputStream()?.use { props.load(it) }

    publishing {
        //Generate sources
        val sourcesJar by tasks.creating(org.gradle.api.tasks.bundling.Jar::class) {
            archiveClassifier.set("sources")
            from(sourceSets.getByName("main").allSource)
        }
        //Generate javadoc
        val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
        dokkaHtml.dependsOn("kaptKotlin")

        val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
            dependsOn(dokkaHtml)
            archiveClassifier.set("javadoc")
            from(dokkaHtml.outputDirectory)
        }

        tasks.withType<Zip>().configureEach {
            doLast {
                listOf("md5", "sha1", "sha-256", "sha-512").forEach {
                    ant.withGroovyBuilder {
                        "checksum"(
                            "file" to this@configureEach.archiveFile.get().asFile,
                            "algorithm" to it
                        )
                    }
                }
            }
        }

        publications {
            create<MavenPublication>(project.name) {
                val gitHubProjectPath = "github.com/nortal/test-automation"
                groupId = "${project.group}"
                artifactId = project.name
                version = version
                from(components["java"])
                artifact(sourcesJar)
                artifact(javadocJar)
                pom {
                    name.set("Test automation framework")
                    description.set("Cucumber based API and UI test framework")
                    url.set("https://$gitHubProjectPath")
                    licenses {
                        license {
                            name.set("MIT license")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    issueManagement {
                        system.set("Github")
                        url.set("https://$gitHubProjectPath/issues")
                    }
                    scm {
                        connection.set("scm:git:git://$gitHubProjectPath.git")
                        developerConnection.set("scm:git:ssh://$gitHubProjectPath.git")
                        url.set("https://$gitHubProjectPath")
                    }
                    developers {
                        developer {
                            name.set("Ričardas Bučiūnas")
                            email.set("ricardas.buciunas@nortal.com")
                            organization.set("Nortal AB")
                            organizationUrl.set("https://nortal.com/")
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                url = if (version.toString().endsWith("SNAPSHOT")) {
                    uri(props.getRequiredProperty("MAVEN_SNAPSHOTS_REPOSITORY_URL", "snapshotsRepoUrl"))
                } else {
                    uri(props.getRequiredProperty("MAVEN_RELEASES_REPOSITORY_URL", "releasesRepoUrl"))
                }
                credentials(org.gradle.api.artifacts.repositories.PasswordCredentials::class) {
                    username = props.getRequiredProperty("OSS_USERNAME", "ossUsername")
                    password = props.getRequiredProperty("OSS_PASSWORD", "ossPassword")
                }
            }
        }
    }

    signing {
        sign(publishing.publications[project.name])
    }
}