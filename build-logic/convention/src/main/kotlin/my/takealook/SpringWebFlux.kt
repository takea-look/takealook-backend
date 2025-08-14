package my.takealook

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

fun Project.configureWebFlux() {
    apply(plugin = libs.findPlugin("spring-boot").get().get().pluginId)
    apply(plugin = libs.findPlugin("spring-dependency-management").get().get().pluginId)
    apply(plugin = libs.findPlugin("kotlin-spring").get().get().pluginId)

    dependencies.apply {
        add("implementation", libs.findLibrary("spring-boot-starter-webflux").get())
        add("testImplementation", libs.findLibrary("spring-boot-starter-test").get())
        add("implementation", libs.findLibrary("kotlinx-coroutines-reactor").get())
        add("implementation", libs.findLibrary("jackson-module-kotlin").get())
    }
}