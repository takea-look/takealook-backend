# Chat REST Controller 설계(초안)

> 이 문서는 `feature/chat` 모듈의 REST 엔드포인트를 한 눈에 보기 위한 설계 요약입니다.

## 목적
- FE가 초기 화면 렌더링에 필요한 데이터(rooms/messages)를 안정적으로 조회
- WS 연결용 티켓 발급을 REST로 제공
- 신고/블라인드 등 운영성 기능을 REST로 제공

## Endpoints

### 1) 채팅방 목록
- `GET /chat/rooms`
  - 응답: `ChatRoom[]`
  - 비고: 정렬/페이징은 별도 스펙 문서에서 확정

### 2) 채팅 메시지 조회/전송
- `GET /chat/rooms/{roomId}/messages?limit={30}&before={createdAt}`
  - 응답: `UserChatMessage[]`
  - reply:
    - `replyToId` + `replyTo(sender,imageUrl,createdAt)`
  - blind:
    - `isBlinded=true`이면 `imageUrl=null`
- `POST /chat/rooms/{roomId}/messages`
  - body: `{ imageUrl, replyToId? }`
  - 응답: `UserChatMessage`
  - text content는 미사용(MVP에서 image-only)

### 기존 호환 엔드포인트
- `GET /chat/messages?roomId={roomId}`
  - 응답: `UserChatMessage[]`
  - 구 스펙 호환용

### 3) WebSocket 티켓 발급
- `POST /chat/ticket`
  - 헤더: `accessToken: <JWT>`
  - 응답: `{ ticket, expiresIn }`

### 4) 메시지 신고/블라인드
- `POST /chat/messages/report?messageId=&reporterUserId=&reason=`
  - 중복 신고 방지: `(messageId, reporterUserId)` unique
  - 누적 10회 이상 시 자동 블라인드

## 관련 문서
- [REST API 계약(초안)](./rest.md)
- [WebSocket 채팅 프로토콜](./websocket-chat.md)
- [WebSocket 티켓 인증 시스템](../architecture/websocket-authentication.md)
