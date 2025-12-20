package my.takealook

import org.gradle.api.Project

fun Project.configureSwagger() {
    dependencies.apply {
        add("implementation", libs.findLibrary("springdoc-openapi-webflux-ui").get())
        add("implementation", libs.findLibrary("webjars-locator-core").get())
    }
}
