jvmApplication {
    jpms {
        moduleName = "io.github.hfhbd.adventofcode.year2023.day3"
    }
    testFixtures {
        dependencies {
            api(testFixtures(project(":year-2023-day1")))
        }
    }
}
