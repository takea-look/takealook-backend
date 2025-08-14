
import my.takealook.configureKotlinModule
import my.takealook.configurePostgreSql
import my.takealook.configureWebFlux
import org.gradle.api.Plugin
import org.gradle.api.Project

class SpringBootLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureKotlinModule()
            configureWebFlux()
            configurePostgreSql()
        }
    }
}
