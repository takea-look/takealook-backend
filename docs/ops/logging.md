# 로깅/모니터링 기본 셋업

## 목표
- 장애 징후를 운영 중에 빠르게 파악할 수 있도록 로그/메트릭/에러 리포트의 최소 축을 마련
- 핵심 비즈니스 흐름(인증, 채팅, 업로드)에서 동작 성과를 메트릭으로 추적

## Structured Logging
- 요청마다 `X-Request-Id`를 부여/전파하고, 로그 패턴에 다음 키를 포함:
  - `requestId`
  - `user` (인증 사용자 식별자)
- 로그 예시
  - `2026-02-17T09:00:00.123+09:00 INFO [main] [rid=abc-123] [user=google_1] ...`

## 장애 추적(에러 리포팅)
- Sentry DSN이 있는 환경에서만 에러 리포팅 활성화
- 로컬 환경은 `SENTRY_DSN` 미설정 시 리포팅 비활성으로 노이즈를 줄임

## 메트릭
- Actuator + Prometheus를 통한 노출
- 주요 커스텀 메트릭:
  - `takealook_auth_requests_total`
  - `takealook_chat_messages_total`
  - `takealook_chat_ws_delivered_total`
  - `takealook_upload_presign_requests_total`
  - `takealook_upload_url_requests_total`
  - `takealook_ws_connections_total`
  - `takealook_ws_rate_limited_total`

## 보기
- `/actuator/health`
- `/actuator/metrics`
- `/actuator/prometheus`

## 운영 가이드
- 공개 노출은 방지하고 내부망/필터 뒤에서 제공
- 로그는 표준 출력으로 수집되며, 운영 로그 어그리게이터(예: CloudWatch, Datadog 등)와 연동 권장
