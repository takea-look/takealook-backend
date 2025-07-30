package my.takealook

import org.gradle.api.Project

fun Project.configurePostgreSql() {
    dependencies.apply {
        add("implementation", libs.findLibrary("spring-data-r2dbc").get())
        add("implementation", libs.findLibrary("r2dbc-pool").get())
        add("implementation", libs.findLibrary("r2dbc-postgresql").get())
    }
}