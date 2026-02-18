# Rate Limiting & Abuse Protection

본 문서는 API 남용 대응을 위한 초기 정책을 정리한다.

## 1. 정책 개요

- 목적: 단기 공격/남용, 브루트포스, 과도한 재전송으로 인한 자원 소모 완화
- 적용 방식:
  - 공개/인증 API는 **scope별 윈도우 기반 요청 상한(슬라이딩 윈도우) + Retry-After 응답**
  - 남용 발생 시 `429 Too Many Requests` 반환 및 `X-RateLimit-*` 정보(내부 구현 한정)
  - 이벤트마다 `takealook_abuse_rate_limited_total` 카운터 적재

## 2. API별 정책 (현재 구현 기준)

| Scope | Endpoint | 인증 | 기본 상한 | 윈도우 | 비고 |
| --- | --- | --- | ---: | ---: | --- |
| auth | `/auth/signin`, `/auth/signup`, `/auth/google`, `/auth/apple`, `/auth/kakao`, `/auth/refresh` | 선택적(로그인 시작 단계는 공개) | 30 req/min | 60s | 키: client identity(IP/headers) 기반 |
| chat-send | `/chat/messages`, `/chat/rooms/{roomId}/messages`(POST) | 필요 | 40 req/min | 60s | room+identity 기반 키(동일 room 반복 spam 억제) |
| upload | `/uploads/presign`, `/storage/upload` | 필요 | 20 req/min | 60s | room/identity 기반 |
| websocket | `/chat/ws` handshake 및 메시지 송신 rate control | 필요 | 60 msg/min | 60s | 연결당/사용자 메세지 flood 방어 |

## 3. 로그인 실패/브루트포스 제어

- 로그인 시도는 `auth` scope에서 모두 카운트한다.
- 로그인 실패, 토큰 갱신 실패, 무효 토큰 호출도 동일 scope 기준으로 제한하여 **단일 계정/단일 IP에서 연속 실패 시 일시적 제한**을 유도한다.
- 계정 고갈 방지를 위해:
  - 클라이언트 메시지에서 제공되는 device 식별값은 최대한 정규화(가능할 경우 사용)
  - 동일 IP에서 연속 실패가 반복될 경우 제한 상수 하향 조정 가능
  - 토큰 재발급 실패/만료 시나리오를 로그/메트릭으로 분리 분석

## 4. 모니터링 & 알림 임계치(권고)

- 핵심 지표:
  - `takealook_abuse_rate_limited_total`
  - `http_server_requests_seconds_count{status="429"}`
  - `takealook_auth_requests_total{result="fail"}`
  - `takealook_ws_rate_limited_total`

- 권고 임계치(시작값):
  1. **전체 5분 기준 429 급증**: `sum(rate(http_server_requests_seconds_count{status="429"}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))`가 baseline 대비 **3x 이상** 또는 절대치 **> 10 req/min**
  2. **로그인 실패 연동 경보**: `sum(rate(takealook_auth_requests_total{result="fail"}[5m])) > 30` (endpoint-level or scope-level)
  3. **WS rate-limit 급증**: `increase(takealook_ws_rate_limited_total[5m]) / clamp_min(increase(takealook_ws_connections_total[5m]), 1) > 0.25`

- 구현 전 점검(PoC/PoI):
  - `abuse.*` 설정값이 실제 traffic에서 의미있게 동작하는지, 스테이징에서 `rate limiting` 상태를 `429` 샘플 + retry-after 응답 헤더/본문으로 확인
  - 위 임계치 경보 룰을 Grafana/Datadog에 먼저 dry-run 적용 후 운영 전환

## 5. 적용 가이드

- 환경변수(`app/src/main/resources/application.properties`):
  - `ABUSE_AUTH_MAX_REQUESTS_PER_MINUTE` (default 30)
  - `ABUSE_AUTH_WINDOW_SECONDS` (default 60)
  - `ABUSE_CHAT_SEND_MAX_REQUESTS_PER_MINUTE` (default 40)
  - `ABUSE_CHAT_SEND_WINDOW_SECONDS` (default 60)
  - `ABUSE_UPLOAD_MAX_REQUESTS_PER_MINUTE` (default 20)
  - `ABUSE_UPLOAD_WINDOW_SECONDS` (default 60)
  - `WS_MAX_MESSAGES_PER_MINUTE`, `WS_RATE_LIMIT_WINDOW_SECONDS`
- 값 튜닝은 환경별로 다르게 운영하되, 로그의 `takealook_abuse_rate_limited_total` 추세를 보고 조정

## 6. 관련 파일

- `app/src/main/resources/application.properties`
- `feature/auth/src/main/kotlin/com/takealook/auth/AuthController.kt`
- `feature/chat/src/main/kotlin/com/takealook/chat/ChatRestController.kt`
- `feature/storage/src/main/kotlin/com/takealook/storage/UploadPresignController.kt`
- `feature/storage/src/main/kotlin/com/takealook/storage/StorageController.kt`
- `core/domain/src/main/kotlin/com/takealook/domain/limiter/AbuseRateLimiter.kt`
