# Chat Message API (Image-only)

> FE-Backend contract for image-only chat message feature.

## 1) Message entity schema

공통 `UserChatMessage` 응답 구조

```json
{
  "messageId": 123,
  "roomId": 1,
  "sender": {
    "id": 10,
    "username": "user1",
    "nickname": "nick",
    "image": "https://.../avatar.png",
    "updatedAt": "2026-02-17T00:00:00"
  },
  "type": "CHAT",
  "imageUrl": "https://cdn/.../img.webp",
  "replyToId": 101,
  "replyTo": {
    "sender": {
      "id": 9,
      "username": "user2",
      "nickname": "alice",
      "image": "https://.../avatar2.png",
      "updatedAt": "2026-02-17T00:00:00"
    },
    "imageUrl": "https://cdn/.../reply.webp",
    "createdAt": 1708100000000
  },
  "isBlinded": false,
  "createdAt": 1708100000000
}
```

### Field 정리
- `image-only`: text/body field 없음, `imageUrl`만 존재 (`null` 허용은 블라인드 예외 케이스)
- `type`: 현재 고정 `CHAT`
- `replyToId`: nullable, 답장인 경우 원본 메시지 id
- `replyTo`: nullable, 답장 메시지 미리보기(발신자, imageUrl, createdAt)
- `isBlinded`: 신고 누적으로 가려진 경우 true면 클라이언트는 `imageUrl` 표시 대신 블라인드 처리
- `createdAt`: epoch milliseconds

## 2) Send endpoint

### POST `/chat/rooms/{roomId}/messages`

**Auth**: JWT Required (`Authorization: Bearer <jwt>`)

**Body**
```json
{ "imageUrl": "https://...", "replyToId": 123 }
```

**Rules**
- `imageUrl` 필수, 이미지 URL만 허용
- `replyToId` optional, 같은 room의 messageId만 유효
- 텍스트/이모지 본문은 미지원(MVP)

**Success (200)**
- 위 `UserChatMessage`(위 schema) 단건 반환

**Errors**
- `401`: 인증 실패
- `403`: room 멤버 아닌 사용자
- `404`: 프로필 없음
- `400`: 잘못된 payload

## 3) List endpoint

### GET `/chat/rooms/{roomId}/messages`

**Auth**: JWT Required

**Query params**
- `limit` (default 30, max 100)
- `before` (optional): cursor 기준(createdAt)
- `beforeMessageId` (optional): cursor 기준(messageId). `beforeMessageId`가 있으면 `before`보다 우선

**Ordering / Paging**
- 기본 정렬: `createdAt desc, messageId desc`
- 기본 페이지: 최신 메시지부터 조회
- 페이지네이션: 요청 시 cursor(시간/ID) 방식
- 페이지 경계: 이전 응답의 마지막 항목의 `messageId`/`createdAt` 사용

**Success (200)**
- `UserChatMessage[]`

## 4) Read receipts

현재 백엔드 REST 계약에 별도 read receipts endpoint는 **미구현**.
- 사용성 요구가 생길 경우 (옵션) `GET /chat/rooms/{roomId}/messages/read-cursor` 또는
  `POST /chat/rooms/{roomId}/messages/read` 형태로 버전별 계약 수립
- 현시점(FE 공유용): read receipts는 **미지원 상태**로 간주

## 5) 공유 규약

- 모든 메시지 ID는 정렬/페이지 이동용 커서로 사용 가능
- `messageId` 기반 커서는 FE가 다음 페이지 요청 시 `beforeMessageId` 사용
- report/blind 정책은 별도 경로 (`POST /chat/messages/report`)로 관리