import dev.detekt.gradle.report.ReportMergeTask

plugins {
    id("io.github.hfhbd.mavencentral.close")
}

tasks.closeMavenCentral {
    namespace.set("io.github.hfhbd")
}

val sarif = configurations.dependencyScope("sarif")
dependencies {
    for (subproject in subprojects) {
        sarif(subproject)
    }
}
val sarifFiles = configurations.resolvable("sarifFiles") {
    extendsFrom(sarif.get())
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("detekt-sarif"))
    }
}

tasks.register("mergeDetektSarif", ReportMergeTask::class) {
    input.from(sarifFiles)
    output.set(layout.buildDirectory.file("reports/detekt/detekt.sarif"))
}
