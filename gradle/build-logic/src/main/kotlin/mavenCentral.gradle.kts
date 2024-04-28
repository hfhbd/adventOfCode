plugins {
    id("maven-publish")
    id("signing")
}

val mavenCentral = configurations.dependencyScope("mavenCentralWorker")
val mavenCentralWorkerClassPath = configurations.resolvable("mavenCentralWorkerClasspath")

dependencies {
    mavenCentral("io.ktor:ktor-client-core:2.3.8")
}

val createMavenCentralZipFile by tasks.registering(Zip::class) {
    archiveFileName.set("$group-${project.name}-${version}.zip")
    destinationDirectory.set(layout.buildDirectory.dir("mavenCentral/publishing"))
}

val publishToMavenCentral by tasks.registering(PublishToMavenCentral::class) {
    uploadZip.set(createMavenCentralZipFile.flatMap {
        it.archiveFile
    })
    workerClassPath.from(mavenCentralWorkerClassPath)
}
