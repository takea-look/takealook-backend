plugins {
    alias(libs.plugins.takealook.spring.library.convention)
    alias(libs.plugins.takealook.feature.module.convention)
}

dependencies {
    implementation(projects.core.domain)
    implementation(libs.aws.s3.core)
    implementation(libs.micrometer.core)

    testImplementation(libs.mockk)
    testImplementation(libs.spring.boot.starter.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Disable bootJar for library module
tasks.named("bootJar") {
    enabled = false
}

tasks.named("jar") {
    enabled = true
}
