aggregate {
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
            for (subproject in subprojects) {
                dokka(project(subproject.path))
            }
        }
    }
}
