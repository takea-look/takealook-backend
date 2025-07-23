plugins {
    alias(libs.plugins.takealook.spring.convention)
}

group = "my.takealook"
version = "0.0.1"

dependencies {
    implementation(projects.feature.icons)
}

tasks.withType<Test> {
    useJUnitPlatform()
}