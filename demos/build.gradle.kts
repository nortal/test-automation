description = "demos"

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