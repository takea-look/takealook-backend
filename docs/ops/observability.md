# 기본 Observability 가이드

## 목표
- 장애 발생 시 로그/메트릭/에러 추적으로 빠르게 원인 추적
- 인증/채팅/업로드 기능 핵심 동작의 건강 상태 가시화
- 운영 환경과 로컬 환경의 에러 리포팅 분리

## 현재 적용 항목
- Structured logging(시작 단계)
  - 공통 요청 헤더 `X-Request-Id`를 발급/전파하고 `MDC`에 `requestId`, `userId`를 넣음
  - 로그 포맷: `%d ... [rid=%X{requestId}] [user=%X{userId}] ...`
- Actuator + Prometheus 연동
  - `/actuator/health`
  - `/actuator/metrics`
  - `/actuator/prometheus`
- Sentry
  - `SENTRY_DSN`이 비어있으면 기본 비활성
  - `SENTRY_ENV`(local/staging/prod) 값으로 환경 구분
- 핵심 커스텀 메트릭
  - `takealook_auth_requests_total`
  - `takealook_chat_messages_total`
  - `takealook_chat_ws_delivered_total`
  - `takealook_upload_presign_requests_total`
  - `takealook_upload_url_requests_total`
  - `takealook_ws_connections_total`
  - `takealook_ws_rate_limited_total`

## 로컬에서 로그 확인
- 애플리케이션 콘솔 로그: `%request` 패턴에 `rid`/`user` 포함
- WebSocket 로그: 핸드셰이크 및 세션별 종료 로그에 `rid`/`user` 태그가 표시됩니다.

## 엔드포인트 조회
- `curl http://localhost:8080/actuator/health`
- `curl http://localhost:8080/actuator/metrics/takealook_auth_requests_total`
- `curl http://localhost:8080/actuator/prometheus`

## 운영 권고
- `actuator`는 기본적으로 내부 네트워크/VPC나 관리자 미들웨어 뒤에서 노출
- `SENTRY_DSN`은 운영/스테이징만 주입하고 로컬은 비워두면 노이즈를 줄일 수 있음
