
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
include(":feature:storage")
include(":feature:push")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic")
