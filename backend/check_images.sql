-- Check if artworks have image data
SELECT 
    id, 
    title, 
    author_id,
    CASE 
        WHEN image_data IS NULL THEN 'NO IMAGE DATA'
        ELSE 'HAS IMAGE DATA (' || LENGTH(image_data) || ' bytes)'
    END as image_status,
    CASE 
        WHEN preview_data IS NULL THEN 'NO PREVIEW DATA'
        ELSE 'HAS PREVIEW DATA (' || LENGTH(preview_data) || ' bytes)'
    END as preview_status,
    image_url,
    preview_url
FROM artworks;
