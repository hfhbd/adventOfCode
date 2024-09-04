pluginManagement {
    repositories {
        repositories {
            maven(url = "https://maven.pkg.github.com/volkswagen-vocs/kfx") {
                name = "GitHubPackages"
                credentials(PasswordCredentials::class)
                mavenContent {
                    includeGroupAndSubgroups("com.volkswagen.vocs.kfx")
                }
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(url = "https://maven.pkg.github.com/volkswagen-vocs/kfx") {
            name = "GitHubPackages"
            credentials(PasswordCredentials::class)
            mavenContent {
                includeGroupAndSubgroups("com.volkswagen.vocs.kfx")
            }
        }
        mavenCentral()
    }
    versionCatalogs.register("libs") {
        from(files("../libs.versions.toml"))
    }
}

rootProject.name = "build-logic"
