plugins {
    id("io.github.hfhbd.mavencentral.close")
    id("merge-detekt")
}

tasks.closeMavenCentral {
    namespace.set("io.github.hfhbd")
}

mergeDetekt {
    dependencies {
        for (subproject in subprojects) {
            sarif(project(subproject.path))
        }
    }
}
