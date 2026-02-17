# takealook-backend
![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/takea-look/takealook-backend?utm_source=oss&utm_medium=github&utm_campaign=takea-look%2Ftakealook-backend&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)

## Getting Started

### Submodule 초기화(Optional)

이 프로젝트는 `takealook-taskmanager`를 서브모듈로 사용하고 있습니다. 프로젝트를 처음 클론하거나 서브모듈을 초기화하려면 다음 명령어들을 실행해주세요:

```bash
git clone --recurse-submodules https://github.com/takea-look/takealook-backend.git

cd takealook-backend
git submodule init
git submodule update --init --recursive
```

### 서브모듈 업데이트

서브모듈의 최신 변경사항을 가져오려면:

```bash
git submodule update --remote --recursive
```

## 사전 준비물

### 1. PostgreSQL 설치 및 user 권한 설정 필요
```
brew install postgresql
brew services start postgresql
```

```sql
CREATE USER admin WITH PASSWORD 'adminpass';
CREATE DATABASE takealook;
GRANT ALL PRIVILEGES ON DATABASE takealook TO admin;
```

### 2. DDL 입력
본 프로젝트는 webflux + r2dbc 기반의 프로젝트이고 JPA같은 ORM이 없습니다. 그렇기에 ddl을 직접 입력해주어야합니다.  
[schema.sql](https://github.com/takea-look/takealook-backend/blob/main/app/src/main/resources/schema.sql)을 실행해주시면됩니다.

### 3. Redis 실행 (로컬 개발)
WebSocket 채팅 인증을 위해 Redis가 필요합니다:

```bash
docker-compose -f docker-compose.local.yml up -d
```

## Run Application

### via Docker Compose (권장)
```bash
docker-compose up -d
```

### via Local Build
```bash
./gradlew :app:bootrun
```

## 환경변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| DB_USERNAME | PostgreSQL 사용자명 | tkladmin |
| DB_PASSWORD | PostgreSQL 비밀번호 | tklpass |
| DB_URL | R2DBC PostgreSQL URL | r2dbc:pool:postgresql://localhost/takealook |
| JWT_SECURE | JWT 서명 키 | (개발용 기본값) |
| R2_ACCOUNT_ID | Cloudflare R2 계정 ID | - |
| R2_ACCESS_KEY | Cloudflare R2 액세스 키 | - |
| R2_SECRET_KEY | Cloudflare R2 시크릿 키 | - |
| R2_BUCKET_NAME | Cloudflare R2 버킷명 | - |
| WS_TICKET_TTL | WebSocket ticket TTL (seconds) | 30 |
| WS_ALLOWED_ORIGINS | WebSocket allowed origins (csv) | http://localhost:5173 |
| REDIS_HOST | Redis 호스트 | localhost |
| REDIS_PORT | Redis 포트 | 6379 |

## Issue triage

이 저장소의 이슈 관리 규칙(최소 기준):

- **Type**: `feat` / `bug` / `chore` 중 1개
- **Priority**: `P0`~`P3` 중 1개
- **Area**: `area/auth`, `area/chat`, `area/upload`, `area/ws`, `area/infra` 중 1개 이상
- **Status**: 필요 시 `status/blocked`, `status/needs-spec` 사용
- **Milestone**: `MVP` 또는 `Post-MVP`로 목표 릴리스 묶기

**운영 규칙(권장):**
1. 긴급도/영향도가 높은 이슈(P0)부터 라벨 정렬 후 담당자 지정
2. 라벨/마일스톤 미지정 이슈는 `needs-spec` 또는 `blocked`로 분류해 TODO로 넘기지 않기
3. 우선순위와 area가 명확해지면 다음 액션(개발/테스트/PR) 계획으로 바로 이어가기


## Kotlin LSP setup

이 저장소는 Kotlin LSP를 사용해서 코드 완성/경고/리팩터링 지원을 받습니다.

- Kotlin LSP 프로젝트: https://github.com/Kotlin/kotlin-language-server

```bash
brew install openjdk
brew install kotlin-language-server
```

VS Code에서 사용할 때:
- 추천 확장: `fwcd.kotlin` (`.vscode/extensions.json`에서 자동 추천)
- `kotlin-language-server` 바이너리를 위 명령으로 설치한 뒤 IDE 재시작

## Documentation

- [WebSocket 티켓 인증 시스템](./docs/architecture/websocket-authentication.md)
- [WebSocket 채팅 연결 가이드](./docs/api/websocket-chat.md)
