# AGENTS.md (feature/auth)

## OVERVIEW
Feature module for JWT-based authentication and security configuration using Spring WebFlux.

## STRUCTURE
```
feature/auth/
├── src/main/kotlin/com/takealook/auth/
│   ├── configuration/      # Security chain and filters
│   ├── component/          # JWT and Auth entry points
│   ├── model/              # DTOs (LoginRequest/Response)
│   ├── AuthController.kt   # Signin/Signup endpoints
│   └── UserController.kt   # Profile/User endpoints
└── build.gradle.kts        # Module build config (jjwt)
```

## WHERE TO LOOK
| Component | File Path | Description |
|-----------|-----------|-------------|
| SecurityConfiguration | `configuration/SecurityConfig.kt` | `SecurityWebFilterChain` & CORS setup |
| AuthController | `AuthController.kt` | Auth endpoints (Calls domain UseCases) |
| AuthService | N/A | Logic is implemented via UseCases in `core:domain` |
| JwtUtil | `component/JwtTokenProvider.kt` | JWT creation, parsing, and validation |
| Auth Filter | `component/JwtAuthenticationFilter.kt` | Reactive filter for JWT extraction |
| Entry Point | `component/JwtAuthenticationEntryPoint.kt` | Handles unauthorized 401 responses |

## CONVENTIONS
- **Reactive Security**: Uses `EnableWebFluxSecurity` for non-blocking I/O.
- **Stateless**: Configured with `NoOpServerSecurityContextRepository`.
- **JWT Provider**: Uses `jjwt` (io.jsonwebtoken) library.
- **Convention**: Applies `takealook.feature.module.convention`.
- **Password**: `BCryptPasswordEncoder` for credential hashing.

## ANTI-PATTERNS
- **NO Blocking Context**: Avoid `SecurityContextHolder.getContext()`; use `ReactiveSecurityContextHolder`.
- **NO State**: Do not use session-based authentication or enable CSRF.
- **Explicit Whitelist**: All new endpoints must be added to `SecurityConfig.kt` if they require public access.
- **Hardcoded Secrets**: Avoid committing JWT secrets; rely on environment variables.

## TESTING
- **Status**: No test files exist yet.
- **Setup**: Configured for JUnit 5 via convention plugins.
