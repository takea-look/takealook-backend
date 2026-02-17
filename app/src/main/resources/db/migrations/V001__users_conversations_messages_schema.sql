-- Migration: add canonical domain tables for user/conversation/message/attachments
-- Issue: #166

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    toss_user_key BIGINT,
    toss_name VARCHAR(255),
    toss_phone VARCHAR(32),
    toss_email VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT PRIMARY KEY,
    username VARCHAR(100),
    nickname VARCHAR(100),
    image_url VARCHAR(255),
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_user_profiles_user
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS conversations (
    id BIGSERIAL PRIMARY KEY,
    created_by_user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    max_participants INT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_conversations_creator
      FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message_type VARCHAR(32) NOT NULL DEFAULT 'CHAT',
    image_url VARCHAR(1024),
    text_content TEXT,
    reply_to_id BIGINT,
    is_blinded BOOLEAN NOT NULL DEFAULT FALSE,
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_messages_conversation
      FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender
      FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_reply_to
      FOREIGN KEY (reply_to_id) REFERENCES messages(id) ON DELETE SET NULL,
    CONSTRAINT chk_messages_type
      CHECK (message_type IN ('CHAT','JOIN','LEAVE','REACTION'))
);

CREATE TABLE IF NOT EXISTS attachments (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    uploaded_by_user_id BIGINT NOT NULL,
    kind VARCHAR(32) NOT NULL DEFAULT 'image',
    file_url VARCHAR(1024) NOT NULL,
    file_name VARCHAR(255),
    mime_type VARCHAR(128) NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes >= 0),
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_attachments_message
      FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_attachments_uploader
      FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_attachment_kind
      CHECK (kind IN ('image','video','file'))
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_toss_user_key ON users (toss_user_key);
CREATE INDEX IF NOT EXISTS idx_conversations_created_by_user ON conversations (created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_messages_conversation_created_desc ON messages (conversation_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_sender_created_desc ON messages (sender_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_attachments_message ON attachments (message_id);

CREATE TABLE IF NOT EXISTS chat_rooms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    max_participants INT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    room_id INT NOT NULL,
    sender_id BIGINT NOT NULL,
    image_url VARCHAR(1024) NOT NULL,
    reply_to_id BIGINT,
    is_blinded BOOLEAN NOT NULL DEFAULT FALSE,
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_chat_messages_room
      FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_sender
      FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_reply
      FOREIGN KEY (reply_to_id) REFERENCES chat_messages(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS chat_room_users (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    room_id INT NOT NULL,
    joined_at BIGINT NOT NULL,
    CONSTRAINT fk_chat_room_users_user
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_room_users_room
      FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
    CONSTRAINT uq_chat_room_users_user_room
      UNIQUE(user_id, room_id)
);

CREATE TABLE IF NOT EXISTS chat_message_reactions (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reaction VARCHAR(64) NOT NULL,
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_chat_message_reactions_message
      FOREIGN KEY (message_id) REFERENCES chat_messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_message_reactions_user
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_chat_message_reactions
      UNIQUE(message_id, user_id, reaction)
);

CREATE TABLE IF NOT EXISTS chat_message_reports (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    reporter_user_id BIGINT NOT NULL,
    reason VARCHAR(255),
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_chat_message_reports_message
      FOREIGN KEY (message_id) REFERENCES chat_messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_message_reports_reporter
      FOREIGN KEY (reporter_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_chat_message_reports
      UNIQUE(message_id, reporter_user_id)
);

CREATE INDEX IF NOT EXISTS idx_chat_rooms_name ON chat_rooms (name);
CREATE INDEX IF NOT EXISTS idx_chat_room_users_room ON chat_room_users (room_id);
CREATE INDEX IF NOT EXISTS idx_chat_room_users_user ON chat_room_users (user_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_room_created_desc ON chat_messages (room_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender_created_desc ON chat_messages (sender_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_chat_message_reactions_message ON chat_message_reactions (message_id);
CREATE INDEX IF NOT EXISTS idx_chat_message_reports_message ON chat_message_reports (message_id);
CREATE INDEX IF NOT EXISTS idx_chat_message_reports_reporter ON chat_message_reports (reporter_user_id);
