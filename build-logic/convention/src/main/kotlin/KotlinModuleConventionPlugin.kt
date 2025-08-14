
import my.takealook.configureKotlinModule
import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureKotlinModule()
        }
    }
}
