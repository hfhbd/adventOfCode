pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://raw.githubusercontent.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin/refs/heads/maven2")
        }
    }
}

plugins {
    id("myRepos")
    id("org.jetbrains.ecosystem").version("0.73.0")
}

rootProject.name = "adventOfCode"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

include(":year-2023-day1")
include(":year-2023-day2")
include(":year-2023-day3")

defaults {
    jvmApplication {
     kotlin {
         compilerOptions {
             jvmTarget = JVM_21
         }
     }

        testFixtures {

        }

        testSuites {
            suites {
                jvmDclTestSuite("test") {
                    foo {

                    }
                }
            }
        }
        detekt {

        }
        dokka {
            sourceSets {
                dclDokkaSourceSet("main") {
                    reportUndocumented = true
                    includes = listOf(layout.projectDirectory.file("README.md"))
                    localDirectory = layout.projectDirectory.dir("src/main/kotlin")
                    remoteUrl = "https://github.com/hfhbd/adventOfCode/tree/main/"
                    remoteLineSuffix = "#L"
                    samples = listOf(layout.projectDirectory.dir("src/test/kotlin"))
                }
            }
        }
        mavenPublish {
            pom {
                name = "hfhbd AdventOfCode"
                description = "hfhbd AdventOfCode"
                url = "https://github.com/hfhbd/adventOfCode"
                license {
                    name = "Apache-2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                }

                developer {
                    id = "hfhbd"
                    name = "Philip Wedemann"
                    email = "mybztg+mavencentral@icloud.com"
                }
                scm {
                    connection = "scm:git://github.com/hfhbd/adventOfCode.git"
                    developerConnection = "scm:git://github.com/hfhbd/adventOfCode.git"
                    url = "https://github.com/hfhbd/adventOfCode"
                }

                distributionManagement {
                    repository {
                        id = "github"
                        name = "GitHub hfhbd Apache Maven Packages"
                        url = "https://maven.pkg.github.com/hfhbd/adventOfCode"
                    }
                }
            }

            publishToGitHubPackages {
                url = "https://maven.pkg.github.com/hfhbd/adventOfCode"
            }
            publishToMavenCentral {
            }

            dokka {

            }
        }
    }

    aggregate {
        mergeDetekt {
        }

        dokka {
            publications {
                includes = listOf(layout.projectDirectory.file("README.md"))
            }
        }
    }
}
