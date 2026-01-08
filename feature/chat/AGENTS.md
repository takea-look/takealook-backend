# CHAT FEATURE KNOWLEDGE BASE

## OVERVIEW
Feature module providing real-time WebSocket messaging and reactive chat room management using Spring WebFlux.

## STRUCTURE
- **Flat Package**: All components reside in `com.takealook.chat` for simplicity.
- **Dependencies**: Leverages `core:domain` for business logic and `core:model` for shared data structures.

## WHERE TO LOOK
| File | Responsibility |
|------|----------------|
| `ChatHandler.kt` | The heart of the module. Handles WebSocket lifecycle, message parsing, persistence via use cases, and broadcasting to active sessions. |
| `ChatConfiguration.kt` | Configures the `HandlerMapping` for the `/chat` endpoint and registers the `WebSocketHandlerAdapter`. |
| `ChatRestController.kt` | Provides GET endpoints for `/chat/rooms` and `/chat/messages` to support UI initialization and history loading. |

## CONVENTIONS
- **Reactive Stack**: strictly uses WebFlux + Kotlin Coroutines. Use `mono { ... }` blocks to bridge between Reactor and Coroutines in the handler.
- **Convention Plugin**: applies `takealook.feature.module.convention` as defined in `build-logic`.
- **WebSocket Handshake**: the client MUST provide `userId` as a query parameter (e.g., `ws://host/chat?userId=1`). This is used for session mapping.
- **Message Protocol**: JSON-based messaging. `ChatMessage` is received from clients, and `UserChatMessage` (enriched with profile data) is broadcasted.
- **Session Management**: currently uses a `ConcurrentHashMap` in `ChatHandler`. Note that this is not distributed; it only tracks users connected to the current instance.

## ANTI-PATTERNS
- **DO NOT** use blocking operations (like JDBC or standard `Thread.sleep`) inside the WebSocket handler.
- **NEVER** ignore errors in the message stream. Use `doOnError` and `onErrorResume` to prevent the entire session from terminating.
- **DO NOT** store sensitive session data directly in the map; only keep the `WebSocketSession` object.
- **AVOID** adding business logic directly in `ChatHandler`. Always delegate to `core:domain` UseCases.

## TESTING STATUS
- **No test files exist yet.**
- **Future testing strategy**:
    - Use `WebTestClient` for the `ChatRestController`.
    - Use `ReactorNettyWebSocketClient` or similar mock tools to verify `ChatHandler` message flow.
    - Focus on verifying that messages are correctly broadcast to all participants in a room.
