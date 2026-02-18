# WebSocket 채팅 프로토콜

> 이 문서는 현재 서버 구현(`feature/chat/ChatHandler.kt`) 기준의 **이벤트 타입/페이로드/재연결/오류 처리**를 정리합니다.

## 0) 전제

- WebSocket 연결은 **티켓 기반(일회용)** 또는 **JWT**로 인증 가능합니다.
- 연결 핸드셰이크는 아래 중 하나의 인증 수단이 필요합니다.
  - Query: `ticket`
  - Query: `token` 또는 `accessToken`
  - Header: `Authorization: Bearer <JWT>`
  - Header: `accessToken: <JWT>` (레거시)
- 핸드셰이크 Query에는 `roomId`가 필수입니다.
- 서버는 `Origin` 헤더를 `ws.allowed-origins`로 검증합니다.
- SSE는 v1 미도입(양방향 브로드캐스트/리액션 동기화를 위해 WS 사용).

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

- `expiresIn` 초 안에 연결해야 하며, 티켓은 소비 후 무효화됩니다.

---

## 2) WebSocket 연결

### URL

```text
wss://{host}/chat?ticket={ticket}&roomId={roomId}
wss://{host}/chat?token={jwt}&roomId={roomId}
wss://{host}/chat?accessToken={jwt}&roomId={roomId}
```

### 연결 실패 코드

- `1002 (POLICY_VIOLATION)`
  - `roomId` 누락
  - Origin 불일치
- `1003 (NOT_ACCEPTABLE)`
  - 티켓 무효/만료
  - JWT 미인증/만료/사용자 조회 실패
  - 방 미참여
- `1007 (BAD_DATA)`
  - 사용자 프로필 조회 실패

---

## 3) Inbound / Outbound 스키마

### 3.1 Client → Server

#### Chat 메시지

```json
{
  "roomId": 1,
  "imageUrl": "https://cdn/sticker.png",
  "replyToId": 12
}
```

#### Reaction

```json
{
  "roomId": 1,
  "messageId": 123,
  "userId": 10,
  "reaction": "❤",
  "action": "add"
}
```

- `action`: `add` | `remove`
- `type`은 선택적으로 전송할 수 있으나, 서버는 기본 판별 규칙으로 동작합니다.

### 3.2 Server → Client

#### `UserChatMessage`

```json
{
  "messageId": 999,
  "roomId": 1,
  "sender": {
    "id": 10,
    "username": "u10",
    "nickname": "철수",
    "image": "https://cdn/u.png"
  },
  "type": "CHAT",
  "imageUrl": "https://cdn/sticker.png",
  "replyToId": null,
  "replyTo": null,
  "isBlinded": false,
  "createdAt": 1708331200000
}
```

- `type`: `CHAT` / `JOIN` / `LEAVE`

#### `UserChatReaction`

```json
{
  "roomId": 1,
  "messageId": 999,
  "userId": 10,
  "reaction": "❤",
  "type": "REACTION",
  "createdAt": 1708331200000
}
```

- `JOIN/LEAVE`는 세션 기준 최초/마지막 연결 변화에서 발송됩니다.

---

## 4) Heartbeat / Ack / Retry 정책(현재 구현 기준)

### 4.1 Heartbeat
- 현재 서버는 WS 앱 레벨 heartbeat payload를 제공하지 않습니다.
- 클라이언트는 브라우저/네트워크 레벨 ping/pong 또는 transport timeout 감시로 keep-alive를 운영합니다.
- 권장: 연결 유지 모니터링 주기 15~30초

### 4.2 Ack 정책
- 현재 서버는 메시지 전송 ACK(전송성공 응답)를 별도 정의하지 않습니다.
- 중복 방지 키는 **`messageId`** (서버가 브로드캐스트 이벤트의 고유 id)로 처리하고, UI는 수신 이벤트 dedupe를 수행합니다.
- `POST /chat/rooms/{roomId}/messages` 호출 응답은 즉시 `UserChatMessage`를 반환해 REST 경로의 저장/ACK 역할을 대체합니다.

### 4.3 Retry 정책

- 공통 재접속 추천(클라이언트):
  - 실패 즉시 재시도하지 말고 `1s, 2s, 4s, 8s, 16s...` (지수 backoff + jitter)
  - 최대 5회 후 네트워크/인증 상태 재확인
- `429` 수신 시: `Retry-After` 초과 후 재시도
- 재접속 시 `ticket`은 재발급 필요(소비형이므로 재사용 불가)

---

## 5) 에러코드 및 클라이언트 처리 규칙

### WebSocket close/error 처리

| 구분 | 코드 | 서버 의미 | 클라이언트 액션 |
|---|---|---|---|
| 정책 위반 | `1002` | roomId 누락 / origin 불일치 | 즉시 재확인 후 재연결(사용자 인터랙션 필요) |
| 인증 실패 | `1003` | 토큰/티켓/권한 문제, 방 미참여 | 토큰/티켓 갱신 뒤 `/chat/ticket` 재발급 후 재접속 |
| 사용자 없음 | `1007` | 사용자 프로필 조회 실패 | 세션 갱신(재로그인) 후 재시도 |

### REST/WS 동시 동기화 오류

- `401`: 인증 토큰 재발급 → `/auth/refresh`, 실패 시 재로그인
- `403`: 방 멤버십 확인 후 UI에서 방 재입장/재조회
- `429`: `Retry-After` 적용, 짧은 재시도 루프 방지

---

## 6) 재연결/정렬/오프라인 백필

- 소켓이 끊기면 `POST /chat/ticket` 후 재연결
- JWT 모드면 토큰 갱신 후 재연결
- 재접속 후 마지막 수신 `messageId` 기준:
  - `GET /chat/rooms/{roomId}/messages?beforeMessageId={lastMessageId}&limit=50`
  - 또는 `before` 기반 커서로 누락 구간 조회
- 수신 이벤트는 `(type, roomId, messageId, createdAt)`으로 정렬/병합

### 중복 수신 방지 규칙(권고)
- 같은 `messageId`를 가진 메시지는 동일 세션/타입에서 1회만 렌더링
- 동일 이벤트를 서버/REST 응답으로 중복 수신 시 `messageId`를 기준으로 ignore

---

## 7) 통합 테스트 계획

### 케이스 1) 연결 끊김 복구
1. 채팅방 연결, 메시지 수신
2. 소켓 강제 close(네트워크 off) 후 재연결
3. 새 티켓 발급 후 연결, 기존 마지막 `messageId`로 백필 API 조회
4. 중간 메시지 누락 없이 정렬 복원 확인

### 케이스 2) 중복 수신
1. 네트워크 지연 중 동일 메시지를 2회 수신 시나리오
2. 클라이언트 캐시에서 `messageId` 중복 필터 동작 확인
3. 메시지 UI 중복 렌더링 0건

### 케이스 3) 무한루프 방지
1. `429` 또는 `1003` 지속 수신 상황에서 클라이언트가 `ticket` 재요청/재연결을 즉시 반복하지 않음
2. 지수 백오프로 재시도 횟수 제한(최대 5회) 후 사용자 안내 표시

### 케이스 4) 규격 오답/권한 오류
1. `roomId` 누락 연결 요청 → `1002` 처리
2. 만료 티켓 재사용 → `1003` 처리
3. 비멤버 사용자 메시지 전송 → REST `403`, WS는 재입장 유도/알림
