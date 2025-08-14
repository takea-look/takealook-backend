plugins {
    alias(libs.plugins.takealook.spring.library.convention)
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.domain)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
