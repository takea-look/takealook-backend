# BUILD-LOGIC KNOWLEDGE BASE

## OVERVIEW
Custom Gradle convention plugins that centralize and enforce build consistency across all modular monolith components.

## WHERE TO LOOK
| File / Plugin | Purpose | Notes |
|------|----------|-------|
| `SpringBootApplicationConventionPlugin` | Configures executable Spring Boot app | Applied to `:app` |
| `SpringBootLibraryConventionPlugin` | Configures reusable Spring Boot library | Applied to feature/core modules |
| `KotlinModuleConventionPlugin` | Base Kotlin setup (JVM 21, strict JSR305) | Foundation for all plugins |
| `FeatureModuleConventionPlugin` | Aggregator for vertical feature modules | Combines Kotlin + feature needs |
| `SpringBootSwaggerConventionPlugin` | Centralized OpenAPI/Swagger configuration | Consistent API docs |
| `my/takealook/Kotlin.kt` | Kotlin compiler and toolchain options | Java 21 target |
| `my/takealook/Libs.kt` | Version catalog accessor extension | Type-safe access to `libs.versions.toml` |
| `my/takealook/PostgreSql.kt` | R2DBC and PostgreSQL client configuration | Reactive DB stack |
| `my/takealook/SpringWebFlux.kt` | WebFlux and Coroutines integration | Core reactive engine |

## CONVENTIONS
- **Plugin Application**: Modules apply logic via ID: `id("takealook.spring.application.convention")`.
- **Centralized Dependencies**: Common dependencies (Coroutines, Reflect, JUnit 5) are injected via `configureKotlinModule()`.
- **Extension-based Config**: Logic is modularized into `Project` extension functions in `my.takealook` package for reuse across plugins.
- **Compiler Safety**: Strict null-safety enforced via `-Xjsr305=strict` compiler argument.

## ANTI-PATTERNS
- **DO NOT** hardcode version numbers - use `gradle/libs.versions.toml` via `libs` accessor.
- **DO NOT** duplicate build configuration in individual `build.gradle.kts` files - create a new extension or plugin here.
- **AVOID** modifying individual module build scripts for global changes - update the corresponding convention plugin instead.
- **NEVER** bypass the JVM 21 toolchain requirement - all modules must stay synchronized on the same Java version.
