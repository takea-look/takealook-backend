plugins {
    alias(libs.plugins.takealook.spring.convention)
}

group = "my.takealook"
version = "0.0.1"

tasks.withType<Test> {
    useJUnitPlatform()
}