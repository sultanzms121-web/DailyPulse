pluginManagement {
    repositories {
        google() // 👈 MUST be here
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // 👈 AND MUST be here
        mavenCentral()
    }
}
rootProject.name = "DailyPulse"
include(":app")