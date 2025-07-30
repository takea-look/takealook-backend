plugins {
    alias(libs.plugins.takealook.spring.convention)
}

dependencies {
    implementation(projects.feature.stickers)
    implementation(projects.feature.auth)
}

tasks.withType<Test> {
    useJUnitPlatform()
}