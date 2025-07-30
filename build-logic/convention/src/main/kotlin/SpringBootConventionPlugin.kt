
import my.takealook.configurePostgreSql
import my.takealook.configureWebFlux
import org.gradle.api.Plugin
import org.gradle.api.Project

class SpringBootConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureWebFlux()
            configurePostgreSql()
        }
    }
}
