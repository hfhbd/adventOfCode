adventOfCode {
    jpms {
        moduleName = "io.github.hfhbd.adventofcode.year2023.day1"
    }

    testing {
        suites {
            jvmDclTestSuite("test") {
                dependencies {
                    implementation(project(":year-2023-day1"))
                }
            }
            jvmDclTestSuite("integrationTest") {
                targets {
                    jvmDclTestSuiteTarget("integrationTest") {
                        javaForkOptions {
                         //   environment("foo", "bar")
                        }
                    }
                    jvmDclTestSuiteTarget("integrationTestProd") {

                    }
                }
                dependencies {
                    implementation(project(":year-2023-day1"))
                }
            }
        }
    }
}
