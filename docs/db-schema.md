# DB Schema (Prisma/ORM mapping baseline)

## Goal
이 파일은 `#166 BE: Database schema for users/conversations/messages` 이슈의 작업 산출물이며, `app/src/main/resources/schema.sql` 기준으로 정리한 관계/제약/인덱스 스냅샷입니다.

## Entities

- `users`
  - PK: `id`
  - Unique: `username`
  - Fields: `username`, `password`, `toss_*`
  - Relation: 부모(1)

- `conversations`
  - PK: `id`
  - FK: `created_by_user_id -> users.id`
  - Fields: `name`, `is_public`, `max_participants`, `created_at`
  - Relation: `users(1) : conversations(N)`

- `messages`
  - PK: `id`
  - FK: `conversation_id -> conversations.id`
  - FK: `sender_id -> users.id`
  - FK: `reply_to_id -> messages.id` (self-ref, `SET NULL`)
  - Fields: `message_type`, `image_url`, `text_content`, `is_blinded`, `created_at`
  - Constraint: `message_type IN ('CHAT','JOIN','LEAVE','REACTION')`
  - Relation: `conversations(1) : messages(N)`, `users(1) : messages(N)`, `messages(1) : messages(N)`

- `attachments`
  - PK: `id`
  - FK: `message_id -> messages.id`
  - FK: `uploaded_by_user_id -> users.id`
  - Fields: `kind`, `file_url`, `file_name`, `mime_type`, `size_bytes`, `created_at`
  - Relation: `messages(1) : attachments(N)`

### Existing runtime tables

- `chat_rooms`, `chat_messages`, `chat_room_users`, `chat_message_reactions`, `chat_message_reports`
  - 이슈 해결 범위 밖이지만 채팅 런타임에서 즉시 사용되고 있으므로 기존 모델은 유지
  - FK 강화 및 조회성능 인덱스만 보강

## 인덱스

- `conversations`: `created_by_user_id`
- `messages`: `(conversation_id, created_at DESC)`, `(sender_id, created_at DESC)`
- `attachments`: `message_id`
- `chat_rooms/messages/users/...`: 기존 조회 패턴 기준 인덱스 추가
  - `chat_messages(room_id, created_at DESC)`, `chat_room_users(room_id)`, `chat_room_users(user_id)` 등

## 적용

- 스키마 엔트리 포인트: `app/src/main/resources/schema.sql`
- `migrations` 폴더는 추후 Flyway/R2DBC Migration 도입 시 `db/migration`로 이관 예정
