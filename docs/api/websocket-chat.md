# WebSocket 채팅 프로토콜

> 이 문서는 현재 서버 구현(`feature/chat/ChatHandler.kt`) 기준의 **이벤트 타입/페이로드**를 정리합니다.

## 0) 전제

- WebSocket 연결은 **티켓 기반(일회용)** 또는 **JWT 인증 토큰**으로 인증 가능합니다.
- WebSocket 핸드셰이크 시 아래 중 하나의 인증 수단을 전달해야 합니다.
  - Query: `ticket`
  - Query: `token` 또는 `accessToken`
  - Header: `Authorization: Bearer <JWT>`
  - Header: `accessToken: <JWT>` (레거시)
- WebSocket 핸드셰이크 시 **query parameter로 `roomId`는 필수** 입니다.
- 서버는 `Origin` 헤더를 `ws.allowed-origins` 설정으로 검증합니다.
- SSE는 1차 스코프에서 미도입(실시간 양방향 리액션/브로드캐스트 반영성상 WS 채택).
  - 정합 규격/전송 정책은 [`realtime-transport.md`](./realtime-transport.md) 기준으로 같이 적용합니다.

---

## 1) 티켓 발급 (HTTP)

### `POST /chat/ticket`

요청 헤더:
- `accessToken: <JWT>`

응답:
```json
{
  "ticket": "550e8400-e29b-41d4-a716-446655440000",
  "expiresIn": 30
}
```

- `expiresIn` 초 안에 연결해야 하며, 티켓은 **소비(consume)되는 일회용**입니다.

---

## 2) WebSocket 연결

### URL (권장)

```text
ws(s)://{host}/chat?ticket={ticket}&roomId={roomId}
```

예시:
```text
wss://api.takealook.app/chat?ticket=550e8400-e29b-41d4-a716-446655440000&roomId=1
```

### URL (JWT fallback)

```text
ws(s)://{host}/chat?token={jwt}&roomId={roomId}
```

예시:
```text
wss://api.takealook.app/chat?roomId=1&accessToken={jwt}
```

헤더 방식도 동일하게 동작합니다.

### 연결 실패(close code)

- `1002 (POLICY_VIOLATION)`
  - `roomId` 누락
  - Origin 불일치(allowed origins 외)
- `1003 (NOT_ACCEPTABLE)`
  - 티켓 무효/만료
  - JWT 토큰 미인증/만료/사용자 조회 실패
- `1007 (BAD_DATA)`
  - 사용자를 찾지 못함

---

## 3) 이벤트 타입

서버가 브로드캐스트하는 메시지는 `type: MessageType` 필드를 포함합니다.

`MessageType`:
- `CHAT` : 일반 채팅 메시지
- `JOIN` : 사용자가 방에 입장(해당 사용자 기준 **첫 세션** 생성 시)
- `LEAVE`: 사용자가 방에서 퇴장(해당 사용자 기준 **마지막 세션** 종료 시)

> 참고: 한 사용자가 여러 디바이스/탭에서 동시에 연결할 수 있으며, `JOIN/LEAVE`는 사용자 세션 수를 기준으로 1번만 발생합니다.

---

## 4) Client → Server 페이로드

서버는 클라이언트가 보낸 텍스트 프레임을 아래 JSON으로 파싱합니다.

### `ChatMessage`
```json
{
  "id": null,
  "roomId": 1,
  "senderId": 123,
  "imageUrl": "https://example.com/sticker.png",
  "replyToId": null,
  "createdAt": 1672531200000
}
```

### `ReactionCommand`
```json
{
  "roomId": 1,
  "messageId": 123,
  "reaction": "LIKE",
  "action": "add"
}
```

필드 설명:
- `id`: 클라이언트에서는 보통 `null` (서버 저장 시 사용)
- `roomId`: 대상 채팅방 ID
- `senderId`: 발신자 사용자 ID
- `imageUrl`: 메시지 이미지 URL (스티커 등). **현재 구현상 필수 문자열**
- `replyToId`: 답장 대상 메시지 ID (없으면 `null`)
- `createdAt`: epoch millis (없으면 서버/클라이언트 기본값에 의해 채워질 수 있음)

---

## 5) Server → Client 페이로드

서버는 수신한 메시지를 저장한 뒤, 방 참여자들에게 아래 형태로 브로드캐스트합니다.

### `UserChatMessage`
```json
{
  "roomId": 1,
  "sender": {
    "id": 123,
    "username": "john",
    "nickname": "길동이",
    "image": "https://...",
    "updatedAt": "2026-02-13T23:00:00"
  },
  "type": "CHAT",
  "imageUrl": "https://example.com/sticker.png",
  "replyToId": null,
  "createdAt": 1672531200000
}
```

- `type`은 `CHAT|JOIN|LEAVE` 중 하나입니다.
- `JOIN/LEAVE` 이벤트의 경우, `imageUrl`은 `null`로 브로드캐스트됩니다.
- 리액션은 별도 `UserChatReaction` 메시지로 전달됩니다.

---

## 6) 재연결/정렬/백필 전략

- 소켓이 끊기면 **다시 `POST /chat/ticket`** 으로 새 티켓 발급 후 재연결하세요.
- JWT 인증 모드라면 `accessToken`/`Authorization` 토큰 갱신 후 재연결하세요.
- 모바일/브라우저에서는 1초~2초 지연 후 **지수 백오프(1, 2, 4, 8초)** 형태로 재시도 권장.
- 순서 보정을 위해 마지막 수신 `messageId`를 기억하고 재접속 후
  `GET /chat/rooms/{roomId}/messages?beforeMessageId={lastMessageId}&limit=50`
  또는 `before` 파라미터로 누락 메시지를 재조회하세요.
