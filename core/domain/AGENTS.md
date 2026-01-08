# AGENTS: core:domain

## OVERVIEW
Pure business logic layer containing subdomains and use cases, strictly decoupled from data implementation and external frameworks.

## STRUCTURE
- `chat/`: Messaging, room management, and chat user logic
- `sticker/`: Sticker discovery and management
- `user/`: User accounts, authentication core, and profile logic
- `exceptions/`: Domain-specific business exceptions (e.g., `ProfileNotFoundException`)

## WHERE TO LOOK
- **UseCases**: `*UseCase.kt` files (e.g., `GetMessagesUseCase.kt`) - Primary entry points for business logic
- **Repositories**: `*Repository.kt` interfaces - Port definitions for data access (implemented in `core:data`)
- **Exceptions**: `com.takealook.domain.exceptions` - Shared business error definitions

## CONVENTIONS
- **Clean Architecture**: Strictly NO dependencies on `core:data` or external frameworks (no Spring annotations)
- **Functional UseCases**: Implement as single-purpose classes using `suspend operator fun invoke()`
- **Port/Adapter Pattern**: Define Repository interfaces here to maintain domain independence
- **Dependency Wiring**: Manual bean registration in `app:TklBeanConfiguration` instead of component scanning
- **Convention Plugin**: Applies `takealook.kotlin.module.convention` (incorrectly referenced as spring library in some docs, but actually uses kotlin module plugin)

## ANTI-PATTERNS
- **DO NOT** add `@Service`, `@Component`, or `@Repository` annotations; keep classes as pure Kotlin POJOs
- **DO NOT** reference `core:data` classes or R2DBC/SQL specific logic
- **DO NOT** skip error handling - use domain-specific exceptions instead of generic ones
- **TESTING GAP**: JUnit 5 is configured but no tests exist yet; prioritize adding unit tests for UseCases
