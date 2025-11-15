pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "sangeetmind-android"

include(
    ":app",
    ":core:common",
    ":core:ui",
    ":core:network",
    ":core:database",
    ":core:audio",
    ":features:onboarding",
    ":features:auth",
    ":features:raaglibrary",
    ":features:player",
    ":features:meditation",
    ":features:astrology",
    ":features:settings",
    ":libs:models",
    ":integration:backend-stub"
)
