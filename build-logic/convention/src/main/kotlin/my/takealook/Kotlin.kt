package my.takealook

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

fun Project.configureKotlinModule() {
    apply(plugin = libs.findPlugin("kotlin-jvm").get().get().pluginId)

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    dependencies.apply {
        add("implementation", libs.findLibrary("kotlinx-coroutines-core").get())
        add("implementation", libs.findLibrary("kotlin-reflect").get())
        add("testImplementation", libs.findLibrary("kotlin-junit5").get())
    }
}