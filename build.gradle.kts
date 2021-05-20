import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
  `java-library`
  `maven-publish`
  kotlin("jvm") version Versions.org_jetbrains_kotlin apply false
  kotlin("plugin.spring") version Versions.org_jetbrains_kotlin apply false
  id("org.springframework.boot") version Versions.org_springframework_boot apply false
}


//bootstrapRefreshVersions()

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
    plugin("com.nortal.test.java-conventions")
  }

  group = "com.nortal.test"

  configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  tasks.withType<Jar> {
    archiveBaseName.set("${rootProject.name}-${project.name}")
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
      jvmTarget = "11"
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
      create<MavenPublication>("${project.name}") {
        groupId = "${project.group}"
        artifactId = "${project.name}"
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
  gradleVersion = Versions.gradleLatestVersion
  distributionType = Wrapper.DistributionType.BIN
}

