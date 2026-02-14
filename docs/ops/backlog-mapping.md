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

- #101 메시지 저장 전략: 5분 캐시(임시 저장) + 조회 API
  - 결론: **5분 캐시 전략은 미채택** (신고/블라인드 근거 데이터 필요로 인해 영구/기간 보존 방향으로 정책 변경)
  - 스펙/정책 문서: docs/api/rest.md (messages 보존/페이징), 관련 이슈 #71

- #96 인증/인가: JWT Guard(Middleware) + 사용자 컨텍스트 주입
  - 구현: JwtAuthenticationFilter(WebFilter)에서 `accessToken` 헤더 기반 JWT 검증 후 ReactiveSecurityContextHolder에 인증 컨텍스트 주입 + SecurityConfig에서 보호 엔드포인트 401 처리
  - 머지 PR: (기존 기능 누적)

- #98 이미지 업로드: Firebase Storage 연동 + 업로드/다운로드 플로우 정의
  - 결론: **Firebase 대신 Cloudflare R2 presigned upload 전략 채택**
  - 구현: GET /storage/upload (key/ext/size/TTL 정책 검증) + img domain/cache policy 문서화
  - 머지 PR: #83(업로드 정책 검증), #84(img domain/cache policy)

