dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://maven.pkg.github.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin")
            name = "dcl"
            credentials(PasswordCredentials::class)
        }
    }
    versionCatalogs.register("libs") {
        from(files("../libs.versions.toml"))
    }
}

rootProject.name = "build-logic"
