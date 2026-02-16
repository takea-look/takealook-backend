# Chat REST Controller 설계(초안)

> 이 문서는 `feature/chat` 모듈 채팅 REST 동작 정리를 목적 기반으로 정리합니다.

## 목적
- FE가 안정적으로 메시지 목록/송신/신고 플로우를 구현할 수 있도록 계약을 고정
- 이미지 전용(`image-only`) 모델에 맞춘 필수 메타데이터/커서 규칙 정리

## Endpoints

### 1) 채팅방 목록
- `GET /chat/rooms`
  - 응답: `ChatRoom[]`
  - Note: 정렬/페이징은 현재 미정(추후 확정)

### 2) 채팅 메시지 조회/전송 (이미지 전용)

#### `GET /chat/rooms/{roomId}/messages`
- 요청:
  - `limit` (default `30`, max `100`)
  - `before` (optional): createdAt cursor
  - `beforeMessageId` (optional): messageId cursor
- 응답: `UserChatMessage[]`
- 정렬: `createdAt desc, messageId desc`
- Note:
  - `beforeMessageId`가 있으면 `before`보다 우선
  - 기본 동작은 최신 메시지부터 역순 페이지네이션

#### `POST /chat/rooms/{roomId}/messages`
- 요청 body: `{ imageUrl, replyToId? }`
- 응답: `UserChatMessage`
- 제약:
  - 텍스트 content는 미사용(MVP에서 image-only)
  - `replyToId`는 같은 room의 메시지만 허용

#### 기존 호환
- `GET /chat/messages?roomId={roomId}`
  - 응답: `UserChatMessage[]`
  - 구 스펙 호환용

## 3) 메시지 신고/블라인드
- `POST /chat/messages/report?messageId={}&reporterUserId={}&reason={}`
  - 중복 신고 방지: `(messageId, reporterUserId)` unique
  - 누적 10회 이상 시 자동 블라인드

## 4) 리드/읽음 처리
- 현재 REST 경로 미구현
- 메시지 리드 상태가 필요할 경우 별도 vNext API 설계 필요 (`POST /chat/rooms/{roomId}/messages/read` 등)
- WS 기반 read receipt도 현재 미구현

## 5) WebSocket 티켓
- `POST /chat/ticket`
  - 헤더: `Authorization: Bearer <JWT>` or `accessToken: <JWT>`
  - 응답: `{ ticket, expiresIn }`

## 관련 문서
- [REST API 계약(초안)](./rest.md)
- [채팅 메시지 계약(이미지-only)](./chat-message-contract.md)
- [WebSocket 채팅 프로토콜](./websocket-chat.md)
- [WebSocket 티켓 인증 시스템](../architecture/websocket-authentication.md)
