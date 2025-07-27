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