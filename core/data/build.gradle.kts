plugins {
    alias(libs.plugins.takealook.spring.convention)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
