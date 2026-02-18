# Chat REST Controller 설계(초안)

> 이 문서는 `feature/chat` 모듈 채팅 REST 동작 정리를 목적 기반으로 정리합니다.

## 목적
- FE가 안정적으로 메시지 목록/송신/리포팅 플로우를 구현할 수 있도록 계약을 고정
- 이미지 전용(`image-only`) 모델에 맞춘 필수 메타데이터/커서 규칙 정리

## 1) 채팅방 API

### `GET /chat/rooms`
- Auth: ✅
- Response: `ChatRoom[]`
- 정렬/페이징: 현재 미정(호환성 유지)

### `POST /chat/rooms`
- Auth: ✅
- Request

```json
{
  "name": "string",
  "isPublic": true,
  "maxParticipants": 0
}
```
- Response: `ChatRoom`

### `GET /chat/rooms/{id}`
- Auth: ✅
- Response: `ChatRoom`

## 2) 채팅 메시지 조회/송신 (이미지 전용)

### `GET /chat/rooms/{roomId}/messages`
- Auth: ✅
- Query
  - `roomId` (path, required)
  - `limit` (optional, default `30`, max `100`)
  - `before` (optional): `createdAt` cursor
  - `beforeMessageId` (optional): messageId cursor
- Response

```json
[
  {
    "messageId": 123,
    "roomId": 1,
    "sender": {
      "id": 1,
      "username": "john",
      "nickname": "존",
      "image": "https://...",
      "updatedAt": "2026-02-13T23:00:00"
    },
    "type": "CHAT",
    "imageUrl": "https://cdn/sticker.png",
    "replyToId": 20,
    "replyTo": {
      "sender": {
        "id": 2,
        "username": "jane",
        "nickname": "제인",
        "image": null,
        "updatedAt": "2026-02-13T22:00:00"
      },
      "imageUrl": "https://cdn/old.png",
      "createdAt": 1672531200000
    },
    "isBlinded": false,
    "createdAt": 1708331200000
  }
]
```

- 정렬: 기본 `createdAt desc, messageId desc`
- 우선순위: `beforeMessageId`가 있으면 `before`보다 우선

### `POST /chat/rooms/{roomId}/messages`
- Auth: ✅
- Request

```json
{
  "imageUrl": "https://cdn/image.png",
  "replyToId": 123   // optional
}
```
- Success (200): `UserChatMessage`
- Error:
  - 401 UNAUTHORIZED (`User not found` / 인증 실패)
  - 403 FORBIDDEN (`User is not a room member`)
  - 429 TOO_MANY_REQUESTS (rate limit 초과, `Retry-After` 헤더 포함)
  - 400 INVALID_REQUEST (예: body 누락/형식 불일치)

### `GET /chat/messages` (legacy)
- Auth: ✅
- Request: `roomId`는 required, `limit`, `before`, `beforeMessageId` optional
- Response: `UserChatMessage[]`
- Note: 구 버전 호환용. 신규 연동은 `/chat/rooms/{roomId}/messages` 사용

## 3) 메시지 신고/블라인드

### `POST /chat/messages/report`
- Auth: ❌ (현재 구현 기준)
- Query: `messageId`, `reporterUserId`, `reason`
- Response: `200 OK`
- 중복 신고 방지: `(messageId, reporterUserId)` unique
- 누적 10회 이상 시 자동 블라인드

## 4) WebSocket 티켓

### `POST /chat/ticket`
- Header: `Authorization: Bearer <JWT>` or `accessToken: <JWT>`
- Response:

```json
{
  "ticket": "550e8400-e29b-41d4-a716-446655440000",
  "expiresIn": 30
}
```

- 1회성, 발급 후 30초 TTL

## 5) 에러/오류 처리 정합 (API 기준)

| 구분 | 코드 | 의미 | FE 대응 |
|---|---:|---|---|
| 인증 실패 | `401` | 토큰/사용자 없음 | 로그인 페이지 또는 토큰 갱신 |
| 비인가 채널 | `403` | 채팅방 멤버 아님 | 채팅방 목록 재동기화 |
| 요청 과부하 | `429` | rate limit 초과 | `Retry-After` 초 후 재시도 |

## 6) 구현 참고

- 리드/읽음 API는 현재 미구현
- WS 기반 read receipt도 미구현
- 텍스트 body는 사용하지 않음 (이미지 URL only)

## 관련 문서
- [REST API 계약(초안)](./rest.md)
- [채팅 메시지 계약(이미지-only)](./chat-message-contract.md)
- [WebSocket 채팅 프로토콜](./websocket-chat.md)
- [WebSocket 티켓 인증 시스템](../architecture/websocket-authentication.md)
