-- DROP TABLE IF EXISTS users CASCADE;
-- DROP TABLE IF EXISTS icons CASCADE;
-- DROP TABLE IF EXISTS icon_categories CASCADE;

CREATE TABLE IF NOT EXISTS icon_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    thumbnail_url VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS icons (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    icon_url VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    category_id INT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES icon_categories(id)
);