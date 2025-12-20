
import my.takealook.configureSwagger
import org.gradle.api.Plugin
import org.gradle.api.Project

class SpringBootSwaggerConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureSwagger()
        }
    }
}
