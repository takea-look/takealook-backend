# REST API 계약(초안)

> 이 문서는 현재 서버 구현 기준으로 REST 엔드포인트와 최소 요청/응답 계약을 정리합니다.
> (세부 정책/페이징/정렬/보존 정책 등은 이 문서에 이어서 확정합니다.)

## Chat

### `GET /chat/rooms`
- 설명: 사용자가 참여 중인 채팅방 목록 조회
- 현재 구현:
  - 요청: (별도 파라미터 없음)
  - 정렬: DB `findAll()` 결과 순서(=정렬 미보장)
  - 페이징: 없음
- 제안 스펙(확정 필요):
  - 정렬: `sort=createdAt_desc|createdAt_asc` (default: `createdAt_desc`)
  - 페이징: `cursor` 기반 또는 `page/limit` 기반 중 택1
  - 필터: `isPublic=true|false` optional
- 응답: `ChatRoom[]`

### `GET /chat/rooms/{roomId}/messages?limit={30}&before={cursor}&beforeMessageId={id}`
- 설명: 특정 채팅방 메시지 내역 조회
- 저장/보존 정책(초안, 확정 필요):
  - 기본: **영구 저장**(신고/블라인드 근거 데이터 필요)
  - 최소 보존: 30~90일(정책 확정 시점에 결정) + 이후는 soft-delete 또는 아카이브
  - 신고/블라인드 연동을 위해 원본 메시지(메타데이터 포함)는 보존 기간 내 조회 가능해야 함
- 쿼리(제안 스펙, 확정 필요):
  - `roomId` (required)
  - `limit` (optional, default 30, max 100)
  - `before` (optional, 메시지 cursor: messageId 또는 createdAt)
- 응답: `UserChatMessage[]`
  - `replyToId`: 답장 대상 메시지 ID
  - `replyTo`: 답장 대상 요약(`sender`, `imageUrl`, `createdAt`) — replyToId가 있을 때 제공
- 정렬(제안): 최신→과거(desc)


### `POST /chat/rooms/{roomId}/messages`
- 설명: 특정 채팅방 이미지 메시지 전송
- 요청 body: `imageUrl`, `replyToId`(optional)
- 응답: `UserChatMessage`

## Storage

### `GET /storage/upload?key={key}&sizeBytes={sizeBytes?}`
- 설명: 파일 업로드용 presigned PUT URL 발급
- key 정책(서버에서 검증):
  - prefix: `chat/`
  - format: `chat/{roomId}/{timestamp}.{ext}`
  - allowed ext: `png|jpg|jpeg|webp`
- sizeBytes 정책(서버에서 사전 검증):
  - `sizeBytes <= 10MB` (기본값)
  - 주의: presigned PUT 자체로 업로드 크기 강제는 어려워 **서버 후처리/검증**이 필요
- TTL: 10분(기본값)

## WebSocket

- WebSocket 계약은 별도 문서 참고:
  - [WebSocket 채팅 프로토콜](./websocket-chat.md)
