
rootProject.name = "takealook-backend"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}


include(":app")

// Core
include(":core:data")
include(":core:domain")

// Feature
include(":feature:stickers")
include(":feature:auth")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic")
