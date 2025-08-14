
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
include(":core:model")

// Feature
include(":feature:stickers")
include(":feature:auth")
include(":feature:chat")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic")
