plugins {
    alias(libs.plugins.takealook.spring.convention)
}

dependencies {
    implementation(projects.core.domain)
}

tasks.withType<Test> {
    useJUnitPlatform()
}