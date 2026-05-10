-- Add balance column to users table if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='users' AND column_name='balance') THEN
        ALTER TABLE users ADD COLUMN balance DOUBLE PRECISION DEFAULT 0.0;
    END IF;
END $$;

-- Update existing users to have balance = 0.0
UPDATE users SET balance = 0.0 WHERE balance IS NULL;

-- Make balance NOT NULL
ALTER TABLE users ALTER COLUMN balance SET NOT NULL;

-- Add ADMIN role if not exists
INSERT INTO roles (name) 
SELECT 'ADMIN' 
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

-- Create admin user (change email and password as needed)
-- Password: admin123 (hashed with BCrypt)
INSERT INTO users (email, username, password_hash, balance, created_at)
SELECT 'admin@artgallery.com', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 0.0, NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@artgallery.com');

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@artgallery.com' 
  AND r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- Also assign USER role to admin
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@artgallery.com' 
  AND r.name = 'USER'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = r.id
  );
