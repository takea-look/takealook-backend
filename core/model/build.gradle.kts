plugins {
    alias(libs.plugins.takealook.kotlin.module.convention)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
