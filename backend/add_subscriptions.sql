-- Создание таблицы подписок
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    subscriber_id BIGINT NOT NULL,
    artist_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscriber FOREIGN KEY (subscriber_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_artist FOREIGN KEY (artist_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_subscription UNIQUE (subscriber_id, artist_id),
    CONSTRAINT check_not_self_subscribe CHECK (subscriber_id != artist_id)
);

-- Создание индексов для оптимизации запросов
CREATE INDEX idx_subscriptions_subscriber ON subscriptions(subscriber_id);
CREATE INDEX idx_subscriptions_artist ON subscriptions(artist_id);
CREATE INDEX idx_subscriptions_created_at ON subscriptions(created_at);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE subscriptions IS 'Таблица подписок пользователей на художников';
COMMENT ON COLUMN subscriptions.id IS 'Уникальный идентификатор подписки';
COMMENT ON COLUMN subscriptions.subscriber_id IS 'ID пользователя, который подписывается';
COMMENT ON COLUMN subscriptions.artist_id IS 'ID художника, на которого подписываются';
COMMENT ON COLUMN subscriptions.created_at IS 'Дата и время создания подписки';
