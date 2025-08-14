plugins {
    alias(libs.plugins.takealook.kotlin.module.convention)
}

dependencies {
    implementation(projects.core.model)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
