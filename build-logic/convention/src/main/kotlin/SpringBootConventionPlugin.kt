
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import kotlin.collections.addAll

class SpringBootConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            apply(plugin = libs.findPlugin("spring-boot").get().get().pluginId)
            apply(plugin = libs.findPlugin("spring-dependency-management").get().get().pluginId)
            apply(plugin = libs.findPlugin("kotlin-jvm").get().get().pluginId)
            apply(plugin = libs.findPlugin("kotlin-spring").get().get().pluginId)

            extensions.configure<JavaPluginExtension> {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(17))
                }
            }

            extensions.configure<KotlinJvmProjectExtension> {
                compilerOptions {
                    freeCompilerArgs.addAll("-Xjsr305=strict")
                }
            }



            dependencies {
                add("implementation", libs.findLibrary("spring-boot-starter-webflux").get())
                add("implementation", libs.findLibrary("spring-data-r2dbc").get())
                add("implementation", libs.findLibrary("r2dbc-pool").get())
                add("implementation", libs.findLibrary("r2dbc-postgresql").get())
                add("implementation", libs.findLibrary("kotlinx-coroutines-core").get())
                add("implementation", libs.findLibrary("kotlinx-coroutines-reactor").get())
                add("implementation", libs.findLibrary("kotlin-reflect").get())
                add("testImplementation", libs.findLibrary("spring-boot-starter-test").get())
                add("testImplementation", libs.findLibrary("kotlin-junit5").get())
                add("implementation", libs.findLibrary("jackson-module-kotlin").get())
            }
        }
    }
}
