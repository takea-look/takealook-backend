# PROJECT KNOWLEDGE BASE

**Generated:** 2026-01-09
**Commit:** (not available)
**Branch:** (not available)

## OVERVIEW
Kotlin Spring Boot modular monolith with WebFlux reactive stack. Chat application with authentication, stickers, and R2 storage.

## STRUCTURE
```
takealook-backend/
├── app/                    # Main entry point, global configuration
├── core/                   # Shared layers
│   ├── model/              # Data models (flat package structure)
│   ├── domain/             # Business logic, use cases
│   └── data/              # R2DBC repositories, mappers
├── feature/                # Vertical features
│   ├── auth/               # JWT authentication
│   ├── chat/               # WebSocket chat, rooms
│   ├── stickers/           # Sticker management
│   └── storage/           # Cloudflare R2 integration
├── build-logic/            # Custom Gradle convention plugins
├── taskmaster/             # AI agent workflow rules
└── gradle/                 # Build configuration
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| App startup | `app/src/main/kotlin/com/takealook/TakealookBackendApplication.kt` | Excludes SecurityAutoConfiguration |
| Bean config | `app/src/main/kotlin/com/takealook/TklBeanConfiguration.kt` | Central wiring |
| Swagger config | `app/src/main/kotlin/com/takealook/SwaggerConfiguration.kt` | OpenAPI docs |
| Database schema | `app/src/main/resources/schema.sql` | Execute manually before first run |
| Convention plugins | `build-logic/convention/src/main/kotlin/` | Module-specific build logic |
| AI rules | `taskmaster/.cursor/rules/` | Cursor/Windsurf agent workflows |
| Dependency versions | `gradle/libs.versions.toml` | Centralized version catalog |

## CONVENTIONS
- **Module organization**: Each module applies convention plugin from `build-logic/`
- **Package structure**: Standard `com.takealook.<module>`, EXCEPT `core:model` uses flat package `com.takealook.model` (single directory)
- **Reactive stack**: WebFlux + R2DBC (non-blocking I/O)
- **Testing**: JUnit 5 configured via convention plugins, but NO test files exist yet
- **Build**: `./gradlew :app:bootrun` (local) or Docker deployment (production)

## ANTI-PATTERNS (THIS PROJECT)
- **NEVER** manually edit `.taskmasterconfig` - use `task-master models` CLI
- **NEVER** assume tool operations succeeded without verification
- **DO NOT** skip DDL initialization - `schema.sql` must be executed manually
- **DO NOT** use environment variables for non-sensitive config - use `.taskmasterconfig`
- **DO NOT** commit secrets - all sensitive data in GitHub Secrets or `.env`

## UNIQUE STYLES
- **Build-logic pattern**: Custom Gradle convention plugins enforce consistency across modules
- **Taskmaster integration**: AI agent rules co-located with backend code in `taskmaster/`
- **Flat package in core:model**: Uses `com.takealook.model` single directory (deviation from standard nested structure)
- **bin/ directories**: Present in source tree (likely build artifacts, should be gitignored)
- **Security exclusion**: `SecurityAutoConfiguration` excluded from main application (custom security setup)

## COMMANDS
```bash
# Local development
./gradlew :app:bootrun

# Docker deployment
docker pull tklcat/takealook-backend:latest
docker run --name app -p 8080:8080 \
  -e DB_USERNAME=... \
  -e DB_PASSWORD=... \
  -e DB_URL=... \
  -e JWT_SECURE=... \
  -e R2_ACCOUNT_ID=... \
  -e R2_ACCESS_KEY=... \
  -e R2_SECRET_KEY=... \
  -e R2_BUCKET_NAME=... \
  tklcat/takealook-backend:latest

# Database setup
# 1. Create PostgreSQL user and database
# 2. Execute app/src/main/resources/schema.sql
```

## NOTES
- **Schema order bug**: `chat_messages` references `chat_rooms` before it's defined (FK constraint order)
- **R2DBC integration**: No ORM (no JPA/Hibernate) - manual SQL required
- **Deployment**: Triggers on version tags (`*.*.*`), builds `linux/arm64` image
- **Git submodules**: Uses `takealook-taskmanager` submodule (see README for init)
- **Testing gaps**: MockK mentioned in PRD but not added to dependencies yet; uses default Mockito
