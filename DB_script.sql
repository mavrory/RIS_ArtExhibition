-- USERS
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ROLES
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- USER_ROLES
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- AUTH PROVIDERS (OAuth)
CREATE TABLE user_auth_providers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL, -- GOOGLE / GITHUB
    provider_user_id VARCHAR(255) NOT NULL,
    UNIQUE(provider, provider_user_id)
);

-- ARTWORK
CREATE TABLE artworks (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2),
    image_url TEXT,
    preview_url TEXT,
    image_data BYTEA,
    preview_data BYTEA,
    is_sold BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- EXHIBITION
CREATE TABLE exhibitions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- EXHIBITION_ARTWORK
CREATE TABLE exhibition_artworks (
    exhibition_id BIGINT REFERENCES exhibitions(id) ON DELETE CASCADE,
    artwork_id BIGINT REFERENCES artworks(id) ON DELETE CASCADE,
    PRIMARY KEY (exhibition_id, artwork_id)
);

-- ORDERS
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    artwork_id BIGINT REFERENCES artworks(id),
    status VARCHAR(50),
    total_price NUMERIC(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- PAYMENTS
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    status VARCHAR(50),
    payment_method VARCHAR(50),
    transaction_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- COMMENTS
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    artwork_id BIGINT REFERENCES artworks(id),
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- FAVORITES
CREATE TABLE favorites (
    user_id BIGINT REFERENCES users(id),
    artwork_id BIGINT REFERENCES artworks(id),
    PRIMARY KEY (user_id, artwork_id)
);

-- INDEXES for performance
CREATE INDEX idx_artworks_author ON artworks(author_id);
CREATE INDEX idx_artworks_sold ON artworks(is_sold);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_artwork ON orders(artwork_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_comments_artwork ON comments(artwork_id);
CREATE INDEX idx_comments_user ON comments(user_id);
CREATE INDEX idx_favorites_user ON favorites(user_id);
CREATE INDEX idx_favorites_artwork ON favorites(artwork_id);
CREATE INDEX idx_exhibitions_created_by ON exhibitions(created_by);

-- SEED DATA: Insert default roles
INSERT INTO roles (name) VALUES ('USER'), ('ARTIST'), ('ADMIN');