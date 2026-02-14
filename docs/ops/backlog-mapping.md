# Backlog Mapping (중복 이슈 정리)

최근 생성된 P0/P1 이슈 중, 이미 구현/머지된 항목을 매핑해 중복 작업을 방지합니다.

## Closed-by mapping

- #103 감정표현(리액션) 데이터 모델 + API/WS 이벤트
  - 구현: chat_message_reactions 테이블 + WS REACTION 커맨드(add/remove) + 브로드캐스트(UserChatReaction)
  - 머지 PR: #85

- #102 신고 API + 10회 이상 신고 시 블라인드 처리
  - 구현: chat_message_reports 테이블(uniq: message_id, reporter_user_id) + 누적 10회 이상 시 is_blinded=true + /chat/messages 응답에 isBlinded 포함(블라인드면 imageUrl null) + POST /chat/messages/report
  - 머지 PR: #87

- #99 WebSocket 채팅: 연결/인증/룸 조인/이미지 메시지 브로드캐스트
  - 구현: /chat/ticket + WsTicketService(REDIS, TTL, getAndDelete consume) + ChatHandler(WebSocket /chat, ticket+roomId, join/leave broadcast, CHAT payload broadcast)
  - 문서: docs/api/websocket-chat.md, docs/architecture/websocket-authentication.md
  - 머지 PR: (기존 기능 누적; 최근 문서 보강 #88)

