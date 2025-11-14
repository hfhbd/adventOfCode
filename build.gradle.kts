import dev.detekt.gradle.report.ReportMergeTask

plugins {
    id("io.github.hfhbd.mavencentral.close")
}

tasks.closeMavenCentral {
    namespace.set("io.github.hfhbd")
}

interface SarifDependencies : Dependencies {
    val sarif: DependencyCollector
}

val sarifDependencies = objects.newInstance<SarifDependencies>()
sarifDependencies.apply {
    for (subproject in subprojects) {
        sarif(dependencyFactory.create(subproject))
    }
}

val sarifFiles = configurations.resolvable("sarifFiles") {
    fromDependencyCollector(sarifDependencies.sarif)
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("detekt-sarif"))
    }
}

tasks.register("mergeDetektSarif", ReportMergeTask::class) {
    input.from(sarifFiles)
    output.set(layout.buildDirectory.file("reports/detekt/detekt.sarif"))
}
