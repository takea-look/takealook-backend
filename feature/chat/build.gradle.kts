plugins {
    alias(libs.plugins.takealook.spring.library.convention)
    alias(libs.plugins.takealook.feature.module.convention)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.feature.auth)

    implementation(libs.spring.boot.starter.data.redis.reactive)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.jjwt.api)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.reactor.test)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}