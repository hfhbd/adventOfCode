val deps = configurations.dependencyScope("mavenCentralClosing")

dependencies {
    deps("io.ktor:ktor-client-java:3.2.1")
    deps("io.ktor:ktor-client-logging:3.2.1")
}

val classpath = configurations.resolvable("mavenCentralClosingClasspath") {
    extendsFrom(deps.get())
}

tasks.register("closeMavenCentral", CloseMavenCentral::class) {
    workerClassPath.from(classpath)
}
