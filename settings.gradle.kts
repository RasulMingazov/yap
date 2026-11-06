pluginManagement {
    includeBuild("convention-plugins")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("$rootDir/libs")
        }
    }
}

rootProject.name = "yap"

include(":admin")
include(":contract-admin")
include(":server:app")
include(":server:core-database")
