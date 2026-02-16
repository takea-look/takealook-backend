# Realtime Transport Contract (WebSocket)

## 1) Protocol decision
- **현재 기준: WebSocket 고정 채택** (단일 채널, bidirectional event 실시간 전송)
- SSE는 도입 보류: 메시지 브로드캐스트/양방향 리액션/재접속 보정 전략 구현 난이도와 비용상 SSE는 1차 스코프로 제외

## 2) Connection & Authentication
- Endpoint: `ws(s)://{host}/chat?roomId={roomId}`
- 인증: 아래 중 하나
  - `ticket`(권장, 1회성/짧은 TTL)
    - `POST /chat/ticket`으로 발급
    - WS `query`로 `ticket` 전달
  - `token`(legacy)/`accessToken` query
  - `Authorization: Bearer <JWT>` 또는 `accessToken` 헤더
- Close code
  - `1002 POLICY_VIOLATION`: `roomId` 누락, Origin 불일치
  - `1003 NOT_ACCEPTABLE`: ticket 무효/만료, 방 미참여, JWT 인증 실패
  - `1007 BAD_DATA`: 사용자 조회 실패

## 3) 이벤트/페이로드
- Client→Server: `CHAT` 메시지는 `ChatMessage` (채팅), `REACTION`은 `ReactionCommand`로 전송
- Server→Client:
  - `UserChatMessage` (`type`: `CHAT/JOIN/LEAVE`) 브로드캐스트
  - `UserChatReaction` (`type` 필드 없음) 브로드캐스트
- FE 정렬 기준: `createdAt`, `messageId` 내림차순/오름차순 일관성 유지

## 4) Ordering / reconnect / backfill
- 실시간 프레임은 최종 전달 보장을 제공하지 않음(네트워크 드롭/재전송은 미지원)
- 클라이언트는 다음 규칙으로 보정:
  1. 로컬에서 마지막으로 본 `messageId`를 기억
  2. WS 재연결 후 `/chat/rooms/{roomId}/messages?beforeMessageId={lastMessageId}&limit=50` 또는 `before`로 이전 구간 조회
  3. 응답 메시지를 `messageId` 기준으로 병합 정렬
- 재연결 권장: 지수 백오프(1s → 2s → 4s → 8s)

## 5) Rate limiting & abuse prevention
- 서버 적용 규칙(현재): 분당 사용자당 최대 60건 전송 허용(반드시 조정 가능)
- 속도 초과 시 해당 세션은 `1002 POLICY_VIOLATION`로 종료
- 추가 방안:
  - 메시지/리액션 모두 동일 슬롯 카운트
  - 동일 `roomId` 다중 세션에서도 사용자 단위 집계

## 6) TODO(차기 스프린트)
- ACK / NACK 이벤트(낙관 전송 재시도) 도입
- 메시지 손실 지표/재시도 큐 및 DLQ 정량화
- 분산 환경 대응(세션 인덱싱/브로드캐스트 스케일링)
