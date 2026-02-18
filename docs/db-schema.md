# DB Schema (Prisma/ORM mapping baseline)

> 출처: `app/src/main/resources/schema.sql` + `app/src/main/resources/db/migrations`

이 문서는 runtime/도메인 모델 관점에서 **현재 기준 스키마(초안)**를 정리한다.

## 핵심 원칙

- `app/src/main/resources/schema.sql`가 논리적 기준 DDL이다.
- `app/src/main/resources/db/migrations`는 실제 적용 순서(bootstrap) 문서 기반으로 관리한다.
- 향후 자동화 마이그레이션 도구(Flyway/Liquibase) 도입 시, 현재 순차 SQL을 기준 migration으로 이관한다.

## Entities (핵심/런타임)

### 1) users
- PK: `id BIGSERIAL`
- Unique: `username`
- fields: `username`, `password`, `toss_user_key`, `toss_name`, `toss_phone`, `toss_email`
- Relation: 부모(1)

### 2) conversations
- PK: `id BIGSERIAL`
- FK: `created_by_user_id -> users.id`
- fields: `name`, `is_public`, `max_participants`, `created_at`
- Relation: `users(1) : conversations(N)`

### 3) messages
- PK: `id BIGSERIAL`
- FK: `conversation_id -> conversations.id`, `sender_id -> users.id`, `reply_to_id -> messages.id (SET NULL)`
- fields: `message_type`, `image_url`, `text_content`, `is_blinded`, `created_at`
- constraints: `message_type IN ('CHAT','JOIN','LEAVE','REACTION')`
- Relation: `conversations(1) : messages(N)`, `users(1) : messages(N)`

### 4) attachments
- PK: `id BIGSERIAL`
- FK: `message_id -> messages.id`, `uploaded_by_user_id -> users.id`
- fields: `kind`, `file_url`, `file_name`, `mime_type`, `size_bytes`, `created_at`
- constraints: `kind IN ('image','video','file')`, `size_bytes >= 0`

### 5) user_profiles
- PK/FK: `user_id -> users.id`
- fields: `username`, `nickname`, `image_url`, `updated_at`

### 런타임 채팅 테이블 (현 운영 테이블)
- `chat_rooms`, `chat_messages`, `chat_room_users`, `chat_message_reactions`, `chat_message_reports`
- 기능 연동상 필요하므로 유지됨(향후 정식 정규화 마이그레이션 시점에서 교체)

## 인덱스

- `users`: `username` unique, `toss_user_key`
- `conversations`: `created_by_user_id`
- `messages`: `(conversation_id, created_at DESC)`, `(sender_id, created_at DESC)`
- `attachments`: `message_id`
- `chat_rooms`: `name`
- `chat_room_users`: `room_id`, `user_id`
- `chat_messages`: `(room_id, created_at DESC)`, `(sender_id, created_at DESC)`
- `chat_message_reactions`: `message_id`
- `chat_message_reports`: `message_id`, `reporter_user_id`

## Migration Baseline

- 초기 마이그레이션 파일은 `app/src/main/resources/db/migrations` 에서 관리한다.
- 현재 실행 순서:
  1. `V001__users_conversations_messages_schema.sql`
  2. `V002__initial_seed.sql`
- 정합/롤백/이행 체크는 `docs/ops/db-migration-baseline.md` 참고.

## 운영 가이드

- 신규 릴리스 이전: 스키마 변경 시 `V###__name.sql` 추가 + `schema.sql` 동기화
- 코드 수정 시 테이블명/컬럼명 변경이 있으면 `docs/db-schema.md`와 엔티티/레포지토리 쿼리 동시 업데이트
- CI에서 Drift는 `docs/ops/db-migration-baseline.md`의 체크 플로우로 검증
