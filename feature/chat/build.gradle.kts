plugins {
    alias(libs.plugins.takealook.spring.convention)
}

dependencies {
    implementation(projects.core.domain)

//    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.websocket)
}

tasks.withType<Test> {
    useJUnitPlatform()
}