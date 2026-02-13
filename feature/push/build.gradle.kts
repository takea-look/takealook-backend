plugins {
    id("takealook.kotlin")
}

dependencies {
    implementation(project(":core:model"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.22")
}
