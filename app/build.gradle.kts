plugins {
    alias(libs.plugins.takealook.spring.library.convention)
    alias(libs.plugins.takealook.spring.swagger.convention)
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.domain)

    implementation(projects.feature.stickers)
    implementation(projects.feature.auth)
    implementation(projects.feature.chat)
    implementation(projects.feature.storage)

    implementation(libs.spring.boot.starter.data.redis.reactive)
}

tasks.withType<Test> {
    useJUnitPlatform()
}