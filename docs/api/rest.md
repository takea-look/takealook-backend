# REST API 계약(초안)

> 이 문서는 현재 서버 구현 기준으로 REST 엔드포인트와 최소 요청/응답 계약을 정리합니다.
> (세부 정책/페이징/정렬/보존 정책 등은 이 문서에 이어서 확정합니다.)

## Chat

### `GET /chat/rooms`
- 설명: 사용자가 참여 중인 채팅방 목록 조회
- 요청: (현재 구현상 별도 파라미터 없음)
- 응답: `ChatRoom[]`

### `GET /chat/messages?roomId={roomId}`
- 설명: 특정 채팅방 메시지 내역 조회
- 쿼리:
  - `roomId` (required)
- 응답: `UserChatMessage[]`
  - `replyToId`: 답장 대상 메시지 ID
  - `replyTo`: 답장 대상 요약(`sender`, `imageUrl`, `createdAt`) — replyToId가 있을 때 제공

## WebSocket

- WebSocket 계약은 별도 문서 참고:
  - [WebSocket 채팅 프로토콜](./websocket-chat.md)
