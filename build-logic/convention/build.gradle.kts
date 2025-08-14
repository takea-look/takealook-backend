import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "my.takealook.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("takealook.spring.application.convention") {
            id = "takealook.spring.application.convention"
            implementationClass = "SpringBootApplicationConventionPlugin"
        }

        register("takealook.spring.library.convention") {
            id = "takealook.spring.library.convention"
            implementationClass = "SpringBootApplicationConventionPlugin"
        }

        register("takealook.kotlin.module.convention") {
            id = "takealook.kotlin.module.convention"
            implementationClass = "KotlinModuleConventionPlugin"
        }

        register("takealook.feature.module.convention") {
            id = "takealook.feature.module.convention"
            implementationClass = "FeatureModuleConventionPlugin"
        }
    }
}
