# API Contract Overview (Auth · Chat · Storage)

> 목적: FE/BE가 동일한 인터페이스 기준으로 작업할 수 있도록 최소 API 계약을 정리
> 기준: 현재 `main` 브랜치 구현 기준 (`2026-02-15`)

## 공통 인증 규칙

- 기본 인증 헤더: `Authorization: Bearer <JWT>`
- 레거시 호환 헤더: `accessToken: <JWT>`
  - `JwtAuthenticationFilter`는 Bearer 우선, 없으면 `accessToken` 헤더로 폴백 처리

## 인증 API (`/auth`)

### MVP 지원 Provider: Google OAuth only

| Method | Path | Auth | Request | Response | Note |
|---|---|---|---|---|---|
| `POST` | `/auth/signin` | ❌ | - | `ErrorResponse` | `410 GONE` (deprecated) |
| `POST` | `/auth/signup` | ❌ | - | `ErrorResponse` | `410 GONE` (deprecated) |
| `POST` | `/auth/google/signin` | ❌ | `GoogleLoginRequest` (`idToken`) | `LoginResponse` (`accessToken`, `refreshToken`) | Google OAuth 로그인 |
| `POST` | `/auth/apple/signin` | ❌ | `GoogleLoginRequest` 형태 | `ErrorResponse` | Apple 미지원(501) |
| `POST` | `/auth/kakao/signin` | ❌ | `GoogleLoginRequest` 형태 | `ErrorResponse` | Kakao 미지원(501) |
| `POST` | `/auth/refresh` | ❌ | `RefreshTokenRequest` (`refreshToken`) | `LoginResponse` (`accessToken`) | refresh 토큰 재발급 |

## 사용자 API (`/user`)

| Method | Path | Auth | Request | Response | Note |
|---|---|---|---|---|---|
| `PATCH` | `/user/profile/me` | ✅ | `{ "nickname"?, "imageUrl"? }` | `UserProfile` | 닉네임은 최초 1회만 변경 가능. 닉네임 길이 2~16, 금칙어 필터, 중복 불가 (`400`/`409`) |
| `GET` | `/user/profile/me` | ✅ | 없음 | `UserProfile` | 현재 로그인 사용자 프로필 |
| `GET` | `/user/profile?userId={id}` | ❌ | Query: `userId` | `UserProfile` | 공개 프로필 조회 |

## 채팅 API (`/chat`)

| Method | Path | Auth | Request | Response | Note |
|---|---|---|---|---|---|
| `GET` | `/chat/rooms` | ✅ | 없음 | `ChatRoom[]` | 참여 채팅방 목록 |
| `POST` | `/chat/rooms` | ✅ | `{ name, isPublic, maxParticipants }` | `ChatRoom` | 채팅방 생성 |
| `GET` | `/chat/rooms/{id}` | ✅ | Path: `id` | `ChatRoom` | 채팅방 단건 조회 |
| `GET` | `/chat/rooms/{id}/messages?limit={30}&before={cursor}&beforeMessageId={id}` | ✅ | Query params | `UserChatMessage[]` | 기본 `limit=30`, 커서 조회 기준: `before` 또는 `beforeMessageId` |
| `POST` | `/chat/rooms/{roomId}/messages` | ✅ | `roomId`, `{ imageUrl, replyToId? }` | `UserChatMessage` | 이미지 URL 기반 메시지 전송. 텍스트 미지원(MVP). |
| `POST` | `/chat/messages/report?messageId={id}&reporterUserId={userId}&reason={reason}` | ❌ | Query params | `200 OK` | 10회 이상 누적시 자동 블라인드 |
| `GET` | `/chat/messages/{id}/reactions` | ✅ | Path: `id` | `[{ reaction, count }]` | `ReactionSummaryItem` 배열 |
| `POST` | `/chat/ticket` | ✅ | 없음 | `WsTicket` (`ticket`, `expiresIn`) | WS 연결 전용 티켓 발급 |

## 저장소 업로드 API (`/storage`)

| Method | Path | Auth | Request | Response | Note |
|---|---|---|---|---|---|
| `GET` | `/storage/upload?key={key}&sizeBytes={size}` | ✅ | Query params | `{ "url": "<presigned upload url>" }` | 기존 하위 호환용. chat 전용 key 규칙: `chat/{roomId}/{timestamp}.{ext}`, 허용 확장자/용량 정책 존재 |
| `POST` | `/uploads/presign` | ✅ | `{ roomId, contentType, sizeBytes? }` | `{ "url": "<presigned upload url>", "key": "chat/{roomId}/{timestamp}.{ext}", "headers": {"Content-Type": "..."} }` | 이미지 업로드 전용. 허용 MIME: `image/png`, `image/jpeg`, `image/jpg`, `image/webp`. Provider: Cloudflare R2 (S3-compatible presigned PUT, 10분 TTL) |

## WebSocket

- WS 엔드포인트: `ws(s)://{host}/chat?ticket={ticket}&roomId={roomId}`
- 연결 전 `POST /chat/ticket` 호출로 ticket 획득 필수
- Inbound 메시지:
  - `ChatMessage` JSON(이미지 메시지 기반)
  - 리액션은 `{ roomId, messageId, userId, reaction, action: add|remove }`
- Outbound 메시지:
  - `UserChatMessage` (CHAT/JOIN/LEAVE)
  - `UserChatReaction`
- **Typing/read receipts**: 현재 구현에서 별도 REST/WS event로 미제공 (MVP TODO)

## 에러 바디 형식(현재 예시)

도메인 예외는 아래 공통 형태(`ErrorResponse`)를 반환.

```json
{
  "status": 401,
  "reason": "INVALID_CREDENTIALS",
  "message": "Invalid username or password"
}
```

## 보완/TODO (실 운영 정합성)

- 업로드 API/타이핑/리드리시트 등 일부 에러는 핸들러 통일이 필요함 (`IllegalArgumentException` → 400 매핑 권장)
- `error code` 정렬 및 문서 버전 표준화는 다음 티켓에서 확정 권장