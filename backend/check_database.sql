-- Check database structure and data

-- 1. Check if artworks table exists and its structure
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'artworks'
ORDER BY ordinal_position;

-- 2. Check if users table exists
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'users'
ORDER BY ordinal_position;

-- 3. Count artworks
SELECT COUNT(*) as total_artworks FROM artworks;

-- 4. Count users
SELECT COUNT(*) as total_users FROM users;

-- 5. Check artworks with their authors
SELECT a.id, a.title, a.author_id, u.username, a.views_count
FROM artworks a
LEFT JOIN users u ON a.author_id = u.id
LIMIT 5;

-- 6. Check for artworks without authors (orphaned)
SELECT COUNT(*) as orphaned_artworks
FROM artworks a
LEFT JOIN users u ON a.author_id = u.id
WHERE u.id IS NULL;
