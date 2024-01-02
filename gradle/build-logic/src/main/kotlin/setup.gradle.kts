plugins {
    kotlin("jvm")
    id("maven-publish")
}

kotlin.jvmToolchain(8)

dependencies {
    testImplementation(kotlin("test"))
}

publishing {
    repositories {
        maven(url = "https://maven.pkg.github.com/hfhbd/advendOfCode") {
            name = "GitHubPackages"
            credentials(PasswordCredentials::class)
        }
    }
    publications.register<MavenPublication>("gpr") {
        from(components["java"])
    }
}
