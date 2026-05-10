-- Добавить колонку balance с значением по умолчанию
ALTER TABLE users ADD COLUMN IF NOT EXISTS balance DOUBLE PRECISION DEFAULT 0.0;

-- Обновить существующих пользователей
UPDATE users SET balance = 0.0 WHERE balance IS NULL;

-- Сделать колонку NOT NULL (опционально)
ALTER TABLE users ALTER COLUMN balance SET NOT NULL;
