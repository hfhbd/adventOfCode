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
        maven(url = "https://maven.pkg.github.com/hfhbd/advendOfCode") {
            name = "GitHubPackages"
            credentials(PasswordCredentials::class)
        }
    }
    publications.register<MavenPublication>("gpr") {
        from(components["java"])
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

signing {
    val signingKey = project.providers.gradleProperty("signingKey")
    val signingPassword = project.providers.gradleProperty("signingPassword")
    if (signingKey.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), signingPassword.get())
        sign(publishing.publications)
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
