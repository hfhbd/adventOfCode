jvmApplication {
    jpms {
        moduleName = "io.github.hfhbd.adventofcode.year2023.day1"
    }

    dependencies {

    }

    testSuites {
        suites {
            jvmDclTestSuite("test") {
                dependencies {
                    implementation(project(":year-2023-day1"))
                }
                foo {

                }
            }
            jvmDclTestSuite("integrationTest") {
                targets {
                    jvmDclTestSuiteTarget("integrationTest") {
                        testing {
                            dependsOnCheck = true
                            javaForkOptions {
                                environment += mapOf(
                                    "foo" to "bar",
                                )
                            }
                        }
                    }
                    jvmDclTestSuiteTarget("integrationTestProd") {
                        testing {
                            dependsOnCheck = false
                        }
                    }
                }
                dependencies {
                    implementation(project(":year-2023-day1"))
                }
            }
        }
    }
}
