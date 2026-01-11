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
| REDIS_HOST | Redis 호스트 | localhost |
| REDIS_PORT | Redis 포트 | 6379 |

## Documentation

- [WebSocket 티켓 인증 시스템](./docs/architecture/websocket-authentication.md)
- [WebSocket 채팅 연결 가이드](./docs/api/websocket-chat.md)
