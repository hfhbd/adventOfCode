plugins {
    kotlin("jvm")
    id("maven-publish")
    id("signing")
}

kotlin.jvmToolchain(8)

dependencies {
    testImplementation(kotlin("test"))
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

java {
    withJavadocJar()
    withSourcesJar()
}

signing {
    val signingKey = providers.gradleProperty("signingKey")
    if (signingKey.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), providers.gradleProperty("signingPassword").get())
        sign(publishing.publications)
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    filePermissions {}
    dirPermissions {}
}

val githubPublications = configurations.consumable("githubPublications") {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("GITHUB_OUTPUT"))
    }
}

val sharePublications by tasks.registering(SharePublications::class) {
    sourceFiles.from(publishing.publications.withType<MavenPublication>().flatMap {
        it.artifacts
    }.map {
        it.file
    })
    targetFile.set(layout.buildDirectory.file("github/publications/outputFiles.txt"))
}

artifacts.add(githubPublications.name, sharePublications)
