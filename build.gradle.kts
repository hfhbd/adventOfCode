plugins {
    id("merge-detekt")
}

mergeDetekt {
    dependencies {
        for (subproject in subprojects) {
            sarif(project(subproject.path))
        }
    }
}
