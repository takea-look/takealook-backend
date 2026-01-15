
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

            tasks.matching { it.name == "bootJar" }.configureEach {
                enabled = false
            }
            tasks.matching { it.name == "jar" }.configureEach {
                enabled = true
            }
        }
    }
}
