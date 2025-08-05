plugins {
    alias(libs.plugins.takealook.spring.convention)
}

dependencies {
    implementation(projects.feature.stickers)
    implementation(projects.feature.auth)
    implementation(projects.feature.chat)
}

tasks.withType<Test> {
    useJUnitPlatform()
}