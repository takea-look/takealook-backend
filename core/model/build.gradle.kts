plugins {
    alias(libs.plugins.takealook.kotlin.module.convention)
}

dependencies {
    api(libs.swagger.annotations)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
