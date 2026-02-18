# DB Migration Baseline & Drift Control

이 문서는 takealook-backend의 초기 DB 스키마/시드 운영 기준을 정리한다.

## 1) 목표

- `schema.sql`의 단일 진실 소스(DDL 기준)를 마이그레이션 순서와 함께 고정
- 로컬/CI에서 스키마 drift를 빠르게 감지
- 초기 데이터(seed)가 있는 경우 재적용 가능한 baseline 절차 제공

## 2) 마이그레이션 소스(실행 순서)

`app/src/main/resources/db/migrations/` 하위는 버전 순 실행:

1. `V001__users_conversations_messages_schema.sql`
   - users/conversations/messages/attachments/stickers/chat runtime tables 생성
   - FK/인덱스 생성 (if not exists)
2. `V002__initial_seed.sql`
   - 기본 스티커 카테고리/샘플 스티커 시드
   - `ON CONFLICT DO NOTHING`으로 재실행 안전

> 현재는 수동 실행 방식이므로 GitHub Action/CD에서 같은 경로의 SQL을 순차 실행하는 정책으로 정렬한다.

## 3) 로컬 bootstrap 순서 (권장)

### 3.1 환경 준비

```bash
createdb takealook
psql "postgresql://tkladmin:tklpass@localhost/takealook"
```

### 3.2 스키마 반영

```bash
psql "postgresql://tkladmin:tklpass@localhost/takealook" -v ON_ERROR_STOP=1 \
  -f app/src/main/resources/db/migrations/V001__users_conversations_messages_schema.sql

psql "postgresql://tkladmin:tklpass@localhost/takealook" -v ON_ERROR_STOP=1 \
  -f app/src/main/resources/db/migrations/V002__initial_seed.sql
```

### 3.3 빠른 정합 확인(선택)

```bash
psql "postgresql://tkladmin:tklpass@localhost/takealook" -c "\dt public.*"
psql "postgresql://tkladmin:tklpass@localhost/takealook" -c "\d+ public.chat_rooms"
```

## 4) 핵심 스키마 ERD (초안)

```mermaid
erDiagram
    USERS ||--o{ USER_PROFILES : has
    USERS ||--o{ CHAT_MESSAGES : sends
    USERS ||--o{ CHAT_MESSAGE_REPORTS : reports
    USERS ||..o{ CHAT_ROOM_USERS : belongs
    USERS ||..o{ CHAT_MESSAGE_REACTIONS : reacts
    USERS ||--o{ CHAT_ROOMS : created_by

    CONVERSATIONS ||--o{ MESSAGES : contains
    MESSAGES ||--o{ MESSAGES : replies_to
    MESSAGES ||--o{ ATTACHMENTS : has
    MESSAGES ||--o{ CHAT_MESSAGE_REPORTS : can_be_reported
    CHAT_MESSAGES ||..o{ CHAT_MESSAGE_REACTIONS : receives
    CHAT_MESSAGES ||--o{ CHAT_MESSAGE_REPORTS : can_be_reported

    CHAT_ROOMS ||--o{ CHAT_MESSAGES : has
    CHAT_ROOMS ||--o{ CHAT_ROOM_USERS : has_member

    STICKER_CATEGORIES ||--o{ STICKERS : contains

    USERS {
        bigint id PK
        varchar username
        varchar password
        bigint toss_user_key
    }
    USER_PROFILES {
        bigint user_id PK FK
        varchar nickname
        varchar image_url
        bigint updated_at
    }
    CONVERSATIONS {
        bigint id PK
        bigint created_by_user_id FK
        varchar name
        bool is_public
        int max_participants
        bigint created_at
    }
    MESSAGES {
        bigint id PK
        bigint conversation_id FK
        bigint sender_id FK
        varchar message_type
        varchar image_url
        text text_content
        bigint reply_to_id FK
        bool is_blinded
        bigint created_at
    }
    ATTACHMENTS {
        bigint id PK
        bigint message_id FK
        bigint uploaded_by_user_id FK
        varchar kind
        varchar file_url
        varchar file_name
        varchar mime_type
        bigint size_bytes
        bigint created_at
    }
    CHAT_ROOMS {
        int id PK
        varchar name
        bool is_public
        int max_participants
        bigint created_at
    }
    CHAT_MESSAGES {
        bigint id PK
        int room_id FK
        bigint sender_id FK
        varchar image_url
        bigint reply_to_id FK
        bool is_blinded
        bigint created_at
    }
    CHAT_ROOM_USERS {
        bigint id PK
        bigint user_id FK
        int room_id FK
        bigint joined_at
    }
    CHAT_MESSAGE_REACTIONS {
        bigint id PK
        bigint message_id FK
        bigint user_id FK
        varchar reaction
        bigint created_at
    }
    CHAT_MESSAGE_REPORTS {
        bigint id PK
        bigint message_id FK
        bigint reporter_user_id FK
        varchar reason
        bigint created_at
    }
    STICKER_CATEGORIES {
        int id PK
        varchar name
        varchar thumbnail_url
    }
    STICKERS {
        int id PK
        varchar name
        varchar icon_url
        varchar thumbnail_url
        int category_id FK
    }
```

## 5) 스키마 Drift 감지(로컬/CI)

### 방법 A: Git 기준 스냅샷 비교(가볍고 빠름)

```bash
DB_URL="postgresql://tkladmin:tklpass@localhost/takealook"

a) 현재 DB 스키마 추출
pg_dump "$DB_URL" --schema-only --no-owner --no-privileges --format=plain \
  > /tmp/takealook_db_dump.sql

b) 기준 스키마 정렬/비교
python3 - <<'PY'
from pathlib import Path

def normalize(path):
    text = Path(path).read_text()
    lines = [ln.strip() for ln in text.splitlines() if ln.strip()]
    return '\n'.join(sorted(lines))

cur = normalize('app/src/main/resources/schema.sql')
db = normalize('/tmp/takealook_db_dump.sql')
print('DIFF' if cur != db else 'NO_DIFF')
PY
```

> `pg_dump`엔 공백/정렬 이슈가 있어 normalize 단계로 오탐을 줄였지만, CI에서는 `schemachange` 또는 `migra` 같은 전용 도구 도입이 바람직.

### 방법 B: 마이그레이션 로그 기반(운영)

- 각 마이그레이션 실행 후 `migration_history` 로그를 남기고, 실행된 버전이 연속인지 감시.
- `V002`의 시드 재적용은 `ON CONFLICT` 보호가 있으므로 중복 실행 가능.

## 6) 초기 migration + seed 롤백 시나리오(예시)

### 6.1 V002 seed 롤백(안전)

```sql
DELETE FROM stickers
WHERE name = '기본 하트'
  AND icon_url LIKE 'https://img.takealook.my/stickers/default/%';

DELETE FROM sticker_categories
WHERE name = '기본 스티커';
```

### 6.2 전체 초기 마이그레이션 롤백(개발 환경)

- 가장 단순하고 확실한 방법: DB 백업 후 drop/recreate

```bash
docker exec -it postgres_local psql -U tkladmin -d takealook -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
```

- 이후 `V001`~`V002` 순차 실행

## 7) CI 체크리스트

- `V001` 실행 성공
- `V002` 실행 성공(중복 실행 2회 시도해도 `ON CONFLICT` 무해성 검증)
- `schema.sql` 텍스트와 `V001` drift 리뷰(필요 시 reviewer 수동 체크)
- 최소 핵심 쿼리/인덱스 존재 여부를 smoke query로 검증

```bash
psql "$DB_URL" -c "SELECT to_regclass('public.chat_messages');"
psql "$DB_URL" -c "SELECT to_regclass('public.sticker_categories');"
psql "$DB_URL" -c "\d public.chat_room_users"
```
