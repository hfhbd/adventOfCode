pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("myRepos")
}

rootProject.name = "adventOfCode"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

include(":year-2023-day1")
include(":year-2023-day2")
include(":year-2023-day3")

defaults {
    adventOfCode {
        testing { }
    }
    foo {
        // testing feature is only available for advendOfCode
        testing {

        }
    }
}
