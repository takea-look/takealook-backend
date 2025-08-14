
import my.takealook.configureKotlinModule
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class FeatureModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureKotlinModule()
        }

        target.dependencies {
            add("implementation", project(":core:domain"))
            add("implementation", project(":core:model"))
        }
    }
}
