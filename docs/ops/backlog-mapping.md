# Backlog Mapping (중복 이슈 정리)

최근 생성된 P0/P1 이슈 중, 이미 구현/머지된 항목을 매핑해 중복 작업을 방지합니다.

## Closed-by mapping

- #103 감정표현(리액션) 데이터 모델 + API/WS 이벤트
  - 구현: chat_message_reactions 테이블 + WS REACTION 커맨드(add/remove) + 브로드캐스트(UserChatReaction)
  - 머지 PR: #85

