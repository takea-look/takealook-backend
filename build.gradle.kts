plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.takealook.spring.application.convention) apply false
    alias(libs.plugins.takealook.spring.library.convention) apply false
    alias(libs.plugins.takealook.kotlin.module.convention) apply false
    alias(libs.plugins.takealook.feature.module.convention) apply false
}