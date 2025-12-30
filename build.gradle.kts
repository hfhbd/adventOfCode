plugins {
    id("merge-detekt")
    id("org.jetbrains.dokka")
}

mergeDetekt {
    dependencies {
        for (subproject in subprojects) {
            sarif(project(subproject.path))
        }
    }
}

dokka {
    dokkaPublications.configureEach {
        includes.from("README.md")
    }

    dependencies {
        for (sub in subprojects) {
            dokka(sub)
        }
    }
}
