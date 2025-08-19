plugins {
    alias(libs.plugins.takealook.spring.library.convention)
    alias(libs.plugins.takealook.feature.module.convention)
}

dependencies {
    implementation(projects.core.domain)

//    implementation(libs.spring.boot.starter.data.redis)
    // webSocket 의존성은 필요없음 (webflux 자체적으로 지원하기 때문)
}

tasks.withType<Test> {
    useJUnitPlatform()
}