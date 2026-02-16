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
    implementation(projects.feature.push)

    implementation(libs.spring.boot.starter.data.redis.reactive)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.sentry.spring.boot.starter)
}
tasks.withType<Test> {
    useJUnitPlatform()
}