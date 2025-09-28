import org.gradle.api.Project.DEFAULT_VERSION

plugins {
    kotlin("jvm")
    id("java-test-fixtures")
    id("maven-publish")
    id("signing")
    id("io.github.hfhbd.mavencentral")
    id("dev.detekt")
}

kotlin.jvmToolchain(21)

testing.suites.withType(JvmTestSuite::class).configureEach {
    useKotlinTest()
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.javadoc {
    onlyIf { false }
}

tasks.compileJava {
    options.javaModuleVersion.set(project.version.toString().takeUnless { it == DEFAULT_VERSION })
}

publishing {
    repositories {
        maven(url = "https://maven.pkg.github.com/hfhbd/adventOfCode") {
            name = "GitHubPackages"
            credentials(PasswordCredentials::class)
        }
        maven(url = "https://central.sonatype.com/repository/maven-snapshots/") {
            name = "mavenCentralSnapshot"
            credentials(PasswordCredentials::class)
        }
        maven(url = "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/") {
            name = "mavenCentralStaging"
            credentials(PasswordCredentials::class)
        }
    }
    publications.register<MavenPublication>("gpr") {
        from(components["java"])
    }
    publications.withType<MavenPublication>().configureEach {
        pom {
            name = "hfhbd AdventOfCode"
            description = "hfhbd AdventOfCode"
            url = "https://github.com/hfhbd/adventOfCode"
            licenses {
                license {
                    name = "Apache-2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }
            developers {
                developer {
                    id = "hfhbd"
                    name = "Philip Wedemann"
                    email = "mybztg+mavencentral@icloud.com"
                }
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
    }
}

signing {
    val signingKey = providers.gradleProperty("signingKey")
    if (signingKey.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), providers.gradleProperty("signingPassword").get())
        sign(publishing.publications)
    }
}

tasks.register<Delete>("deleteDetektBaseline") {
    delete(tasks.detekt.flatMap { it.baseline })
}

tasks.detekt {
    ignoreFailures = providers.gradleProperty("ignoreDetektFailures").map { it.toBoolean() }.orElse(false)
}

val sarif = configurations.consumable("sarif") {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("detekt-sarif"))
    }
}

artifacts.add(sarif.name, tasks.detekt.flatMap { it.reports.sarif.outputLocation })
