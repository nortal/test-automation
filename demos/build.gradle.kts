import org.jetbrains.dokka.gradle.DokkaTask

description = "demos"

allprojects {
    tasks.withType<Jar>().configureEach { enabled = false }
    tasks.withType<PublishToMavenRepository>().configureEach { enabled = false }
    tasks.withType<PublishToMavenLocal>().configureEach { enabled = false }
    tasks.withType<DokkaTask>().configureEach { enabled = false }
    tasks.withType<Sign>().configureEach { enabled = false }
    tasks.named("javadocJar").configure { enabled = false }
}

subprojects {
    apply {
        plugin("java-library")
        plugin("io.freefair.lombok")
    }

    tasks.withType<Test> {
        if (project.hasProperty("skipDemoTests")) {
            exclude("**/**")
        }
    }
}