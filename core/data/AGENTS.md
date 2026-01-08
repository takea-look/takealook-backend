# AGENTS: core:data

## OVERVIEW
Reactive data access layer utilizing R2DBC for high-performance, non-blocking PostgreSQL operations with Kotlin Coroutines.

## STRUCTURE
- `chat/`: Persistence for messages, rooms, and room membership.
- `sticker/`: Management of sticker metadata and categories.
- `user/`: Core user account and profile persistence.
- Each domain package follows the pattern:
    - `*Entity.kt`: R2DBC table mapping using Spring Data Relational.
    - `*R2dbcRepository.kt`: `CoroutineCrudRepository` for reactive DB access.
    - `*RepositoryImpl.kt`: Domain interface implementation with mappers.

## WHERE TO LOOK
- **Repositories**: `*RepositoryImpl.kt` - Implementation of domain ports (e.g., `ChatMessagesRepositoryImpl.kt`).
- **R2DBC Interfaces**: `*R2dbcRepository.kt` - Reactive repository definitions with `@Query` support.
- **Mappers**: `toExternal()` and `fromExternal()` extension functions within `*Entity.kt` files.
- **Schema**: `app/src/main/resources/schema.sql` - Source of truth for database structure.

## CONVENTIONS
- **Reactive Stack**: Exclusively use `suspend` functions and `CoroutineCrudRepository`. No blocking I/O.
- **Manual SQL**: Use `@Query` for non-standard operations (INSERT/UPDATE/Complex JOINs) as R2DBC lacks standard ORM features.
- **Port/Adapter Implementation**: Implement Repository interfaces defined in `core:domain`.
- **Domain Decoupling**: Maintain strict separation between `*Entity` and domain models via explicit mapping.
- **Convention Plugin**: Applies `takealook.spring.library.convention` with R2DBC and PostgreSQL drivers.

## ANTI-PATTERNS
- **NO JPA/Hibernate**: Do not use `@Entity` (JPA) or `@OneToMany`. Use `org.springframework.data.relational.core.mapping.Table`.
- **No Blocking Calls**: Avoid `.block()` or traditional JDBC drivers at all costs.
- **Domain Contamination**: Never leak `*Entity` classes outside this module; always map to domain models.
- **Auto-DDL Reliance**: Do not expect Hibernate auto-create; all schema changes must be updated in `schema.sql`.
