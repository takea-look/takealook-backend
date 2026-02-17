# 운영/배포 환경변수 정리

> 신규 개발자가 로컬/스테이밍을 바로 띄울 수 있도록 **필수/선택 환경변수**를 정리합니다.

## 필수(로컬 개발 최소)

### Java / Gradle toolchain
- 이 프로젝트는 **JDK 21**이 필요합니다: [로컬 개발: JDK 21 세팅 가이드](./jdk21.md)

### Auth header
- 표준: `Authorization: Bearer <JWT>`
- 레거시(호환): `accessToken: <JWT>`

### DB (PostgreSQL / R2DBC)
- `DB_URL` (default: `r2dbc:pool:postgresql://localhost/takealook`)
- `DB_USERNAME` (default: `tkladmin`)
- `DB_PASSWORD` (default: `tklpass`)

### JWT
- `JWT_SECURE` (default: dev fallback 존재)
- `JWT_EXP` (default: `3600000`)

### Redis (WS 티켓)
- `REDIS_HOST` (default: `localhost`)
- `REDIS_PORT` (default: `6379`)

### WebSocket
- `WS_TICKET_TTL` (default: `30` seconds)
- `WS_ALLOWED_ORIGINS` (default: `http://localhost:5173`)
- `WS_MAX_MESSAGES_PER_MINUTE` (default: `60`)
- `WS_RATE_LIMIT_WINDOW_SECONDS` (default: `60`)

## 선택(스토리지 업로드 기능 사용 시)

### Cloudflare R2
- `R2_ACCOUNT_ID`
- `R2_ACCESS_KEY`
- `R2_SECRET_KEY`
- `R2_BUCKET_NAME`

추가 정책(서버 검증):
- presign TTL: `cloud.r2.presignTtlMinutes` (default 10)
- max upload size: `cloud.r2.maxUploadBytes` (default 10MB)
- allowed extensions: `cloud.r2.allowedExtensions` (default png/jpg/jpeg/webp)
- allowed key prefix: `cloud.r2.allowedKeyPrefix` (default `chat/`)
- public image base URL: `cloud.r2.public-base-url` (default `https://img.takealook.my`)

## 선택(Toss 인증)
- `TOSS_DECRYPTION_KEY`

## 로깅/모니터링(최소)
- 서버 로그는 기본적으로 표준 출력으로 남깁니다.
- 운영에서는 Cloud provider 로그 수집(예: CloudWatch/Stackdriver/Datadog 등)에 연결 권장.

## Observability/Monitoring
- `MANAGEMENT_ENDPOINTS` (default: `health,info,prometheus,metrics`)
- `MANAGEMENT_HEALTH_DETAILS` (default: `never`)
- `MANAGEMENT_HEALTH_PROBES` (default: `false`)
- `SENTRY_DSN` (empty = 비활성)
- `SENTRY_ENV` (default: `local`)
- `SENTRY_TRACES_SAMPLE_RATE` (default: `0.0`)
- `SENTRY_SEND_PII` (default: `false`)
- 요청 ID: `X-Request-Id` (미입력 시 서버가 생성)

## Abuse Protection
- `ABUSE_AUTH_MAX_REQUESTS_PER_MINUTE` (default: `30`)
- `ABUSE_AUTH_WINDOW_SECONDS` (default: `60`)
- `ABUSE_CHAT_SEND_MAX_REQUESTS_PER_MINUTE` (default: `40`)
- `ABUSE_CHAT_SEND_WINDOW_SECONDS` (default: `60`)
- `ABUSE_UPLOAD_MAX_REQUESTS_PER_MINUTE` (default: `20`)
- `ABUSE_UPLOAD_WINDOW_SECONDS` (default: `60`)
- `TAKEALOOK` rate-limit abuse events metric: `takealook_abuse_rate_limited_total` (scope별 라벨: scope, endpoint)
