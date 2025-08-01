plugins {
    alias(libs.plugins.takealook.spring.convention)
}

dependencies {
    implementation(projects.core.data)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
