jvmApplication {
    jpms {
        moduleName = "io.github.hfhbd.adventofcode.year2023.day1"
    }

    dependencies {

    }

    testSuites {
        suites {
            jvmTestSuite("test") {
                dependencies {
                    implementation(project())
                }
                useKotlinTest {

                }
            }
            jvmTestSuite("integrationTest") {
                useKotlinTest {
                    version = "2.4.0-Beta2"
                }

                targets {
                    jvmTestSuiteTarget("integrationTest") {
                        testing {
                            dependsOnCheck = true
                            javaForkOptions {
                                environment += mapOf("foo" to "bar")
                            }
                        }
                    }
                    jvmTestSuiteTarget("integrationTestProd") {
                        testing {
                            dependsOnCheck = false
                        }
                    }
                }
                dependencies {
                    implementation(project())
                }
            }
        }
    }
}
