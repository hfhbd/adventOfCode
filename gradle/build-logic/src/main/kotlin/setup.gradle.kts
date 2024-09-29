import dev.sigstore.sign.tasks.SigstoreSignFilesTask

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("signing")
    id("dev.sigstore.sign")
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

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    filePermissions {}
    dirPermissions {}
}

tasks.withType(SigstoreSignFilesTask::class).configureEach {
    launcher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    })
}
