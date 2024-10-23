plugins {
    kotlin("jvm")
    id("maven-publish")
}

kotlin.jvmToolchain(8)

dependencies {
    testImplementation(kotlin("test"))
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven(url = "https://maven.pkg.github.com/hfhbd/adventOfCode") {
            name = "GitHubPackages"
            credentials(PasswordCredentials::class)
        }
    }
    publications.register<MavenPublication>("gpr") {
        from(components["java"])
    }
    publications.withType<MavenPublication>().configureEach {
        this.pom {
            // https://github.com/gradle/gradle/issues/28759
            this.withXml {
                this.asNode().appendNode("distributionManagement").appendNode("repository").apply {
                    this.appendNode("id", "github")
                    this.appendNode("name", "GitHub hfhbd Apache Maven Packages")
                    this.appendNode("url", "https://maven.pkg.github.com/hfhbd/adventOfCode")
                }
            }
        }
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    filePermissions {}
    dirPermissions {}
}

configurations.consumable("githubPublications") {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("GITHUB_OUTPUT"))
    }
    outgoing {
        artifacts(provider {
            publishing.publications.withType<MavenPublication>().flatMap {
                it.artifacts
            }.map {
                it.file
            }
        })
    }
}
