# takealook-backend
![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/takea-look/takealook-backend?utm_source=oss&utm_medium=github&utm_campaign=takea-look%2Ftakealook-backend&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)
## Getting Started

### Submodule 초기화(Obtional)

이 프로젝트는 `takealook-taskmanager`를 서브모듈로 사용하고 있습니다. 프로젝트를 처음 클론하거나 서브모듈을 초기화하려면 다음 명령어들을 실행해주세요:

```bash
# 저장소 클론 (처음 클론하는 경우)
git clone --recurse-submodules https://github.com/takea-look/takealook-backend.git

# 또는 이미 클론한 경우
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
### 1. postgresSQL 설치 및 user 권한 설정 필요
```
brew install postgresql
brew services start postgresql
```

```sql
-- 사용자 생성
CREATE USER admin WITH PASSWORD 'adminpass';

-- 데이터베이스 생성
CREATE DATABASE takealook;

-- 권한 부여
GRANT ALL PRIVILEGES ON DATABASE takealook TO admin;
```

### 2. DDL 입력
본 프로젝트는 webflux + r2dbc 기반의 프로젝트이고 JPA같은 ORM이 없습니다. 그렇기에 ddl을 직접 입력해주어야합니다.  
[schema.sql](https://github.com/takea-look/takealook-backend/blob/main/app/src/main/resources/schema.sql)을 실행해주시면됩니다.

## run application via Docker
```sh
docker pull tklcat/takealook-backend:latest
docker run --name app -p 8080:8080 \
    -e DB_USERNAME=ENTER_YOUR_POSTGRESQL_USERNAME \
    -e DB_PASSWORD=ENTER_YOUR_POSTGRESQL_PASSWORD \
    -e DB_URL=ENTER_YOUR_POSTGRESQL_DB_URL \
    -e JWT_SECURE=YOUR_JWT_SECURE \
    -e R2_ACCOUNT_ID=YOUR_R2_ACCOUNT_ID \
    -e R2_ACCESS_KEY=YOUR_R2_ACCESS_KEY \
    -e R2_SECRET_KEY=YOUR_R2_SECRET_KEY \
    -e R2_BUCKET_NAME=YOUR_R2_BUCKET_NAME \
    takealook-backend:latest > tkl.log 2<&1 &
```

## run application via local build
```
./gradlew :app:bootrun
```
