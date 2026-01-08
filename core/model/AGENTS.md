# core:model KNOWLEDGE BASE

## OVERVIEW
Data models serving as both entities and DTOs for the entire project, using a simplified flat package structure.

## WHERE TO LOOK
| Model | File | Description |
|-------|------|-------------|
| ChatMessage | `ChatMessage.kt` | Core message data including sender and room IDs |
| ChatRoom | `ChatRoom.kt` | Room metadata (name, public status, participants) |
| ChatUser | `ChatUser.kt` | Mapping between users and chat rooms |
| Sticker | `Sticker.kt` | Sticker metadata and resource URL |
| StickerCategory | `StickerCategory.kt` | Grouping for stickers |
| User | `User.kt` | Account credentials and profile information |
| UserChatMessage | `UserChatMessage.kt` | Combined message and user profile for UI |

## CONVENTIONS
- **Flat Package Structure**: Uses `com.takealook.model` as a single directory name in `src/main/kotlin/` (no nested directories).
- **Hybrid Responsibility**: Data classes act as both domain entities and OpenAPI DTOs using `@Schema` annotations.
- **Convention Plugin**: Applies `takealook.kotlin.module.convention` via `build.gradle.kts`.
- **Immutability**: Preference for `data class` with `val` properties.

## ANTI-PATTERNS
- **NEVER** create nested sub-packages; maintain the flat structure for all models in this module.
- **DO NOT** create separate DTO classes unless there is a significant divergence from the base model.
- **AVOID** adding business logic; keep these classes as pure data holders.
