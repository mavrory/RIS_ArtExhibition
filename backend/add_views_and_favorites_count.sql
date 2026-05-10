-- Add views_count column to artworks table if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='artworks' AND column_name='views_count') THEN
        ALTER TABLE artworks ADD COLUMN views_count INTEGER DEFAULT 0;
        RAISE NOTICE 'Column views_count added to artworks table';
    ELSE
        RAISE NOTICE 'Column views_count already exists in artworks table';
    END IF;
END $$;

-- Update existing artworks to have views_count = 0 if NULL
UPDATE artworks SET views_count = 0 WHERE views_count IS NULL;

-- Add index for views_count for performance
CREATE INDEX IF NOT EXISTS idx_artworks_views ON artworks(views_count);

-- Verify the column exists
SELECT column_name, data_type, column_default 
FROM information_schema.columns 
WHERE table_name = 'artworks' AND column_name = 'views_count';
