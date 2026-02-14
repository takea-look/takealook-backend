# 운영/배포 환경변수 정리

> 신규 개발자가 로컬/스테이징을 바로 띄울 수 있도록 **필수/선택 환경변수**를 정리합니다.

## 필수(로컬 개발 최소)

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

## 선택(Toss 인증)
- `TOSS_DECRYPTION_KEY`

## 로깅/모니터링(최소)
- 서버 로그는 기본적으로 표준 출력으로 남깁니다.
- 운영에서는 Cloud provider 로그 수집(예: CloudWatch/Stackdriver/Datadog 등)에 연결 권장.
