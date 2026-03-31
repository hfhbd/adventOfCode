aggregate {
    mergeDetekt {
        dependencies {
            for (subproject in subprojects) {
                sarif(project(subproject.path))
            }
        }
    }

    dokka {
        dependencies {
            for (subproject in subprojects) {
                dokka(project(subproject.path))
            }
        }
    }
}
