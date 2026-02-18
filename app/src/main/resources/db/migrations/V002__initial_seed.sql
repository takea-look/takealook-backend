-- Seed baseline data for local/CI bootstrap.
-- Uses idempotent logic so it can be re-run safely.

INSERT INTO sticker_categories (name, thumbnail_url)
SELECT
  '기본 스티커',
  'https://img.takealook.my/stickers/categories/default.png'
WHERE NOT EXISTS (
    SELECT 1 FROM sticker_categories WHERE name = '기본 스티커'
);

INSERT INTO stickers (name, icon_url, thumbnail_url, category_id)
SELECT
  '기본 하트',
  'https://img.takealook.my/stickers/default/heart.png',
  'https://img.takealook.my/stickers/default/heart-thumb.png',
  c.id
FROM sticker_categories c
WHERE c.name = '기본 스티커'
  AND NOT EXISTS (
    SELECT 1
    FROM stickers s
    WHERE s.name = '기본 하트' AND s.category_id = c.id
  );

-- keep a deterministic row in case API consumers expect at least one sample sticker.