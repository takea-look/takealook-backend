plugins {
    alias(libs.plugins.takealook.spring.library.convention)
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.domain)

    implementation(projects.feature.stickers)
    implementation(projects.feature.auth)
    implementation(projects.feature.chat)
}

tasks.withType<Test> {
    useJUnitPlatform()
}