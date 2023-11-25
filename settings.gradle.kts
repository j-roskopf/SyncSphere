rootProject.name = "Sync Sphere"

include(":androidApp")
include(":shared")
include(":desktopApp")

pluginManagement {
    includeBuild("gradle/build-logic")

    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
