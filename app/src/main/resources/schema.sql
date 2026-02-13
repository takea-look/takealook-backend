CREATE TABLE IF NOT EXISTS sticker_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    thumbnail_url VARCHAR(1024) NOT NULL
);

CREATE TABLE IF NOT EXISTS stickers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    icon_url VARCHAR(1024) NOT NULL,
    thumbnail_url VARCHAR(1024) NOT NULL,
    category_id INT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES sticker_categories(id)
);

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT PRIMARY KEY,
    username VARCHAR(100),
    nickname VARCHAR(100),
    image_url VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    room_id INT NOT NULL REFERENCES chat_rooms(id),
    sender_id INT NOT NULL REFERENCES users(id),
    image_url VARCHAR(1024) NOT NULL,
    reply_to_id BIGINT REFERENCES chat_messages(id),
    is_blinded BOOLEAN NOT NULL DEFAULT FALSE,
    created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_message_reports (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES chat_messages(id) ON DELETE CASCADE,
    reporter_user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason VARCHAR(255),
    created_at BIGINT NOT NULL,
    UNIQUE(message_id, reporter_user_id)
);

CREATE TABLE IF NOT EXISTS chat_rooms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    max_participants INT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_room_users(
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    room_id INT NOT NULL REFERENCES chat_rooms(id),
    joined_at BIGINT NOT NULL,
    UNIQUE(user_id, room_id)
);

CREATE TABLE IF NOT EXISTS chat_message_reactions (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES chat_messages(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reaction VARCHAR(64) NOT NULL,
    created_at BIGINT NOT NULL,
    UNIQUE(message_id, user_id, reaction)
)