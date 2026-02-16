import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.takealook.spring.library.convention)
    alias(libs.plugins.takealook.feature.module.convention)
}

group = "my.takealook"
version = "0.0.1"

dependencies {
    implementation(projects.core.domain)

    implementation(libs.spring.boot.starter.security)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    implementation(libs.nimbus.jose.jwt)

    implementation(libs.micrometer.core)

    // Toss API dependencies
    implementation(libs.okhttp)
    implementation(libs.bouncycastle)
    implementation(libs.jackson.module.kotlin)

    testImplementation(libs.mockk)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
}

tasks.named("bootJar") {
    enabled = false
}

tasks.named("jar") {
    enabled = true
}
