-- Fix hardcoded image paths in user_profiles table
-- This script removes any hardcoded file paths and sets them to NULL
-- so the system will use default avatars instead

UPDATE `user_profiles` 
SET `profile_photo_url` = NULL 
WHERE `profile_photo_url` LIKE '%wael%' 
   OR `profile_photo_url` LIKE '%C:/Users/%' 
   OR `profile_photo_url` LIKE '%C:\\Users\\%'
   OR `profile_photo_url` LIKE 'file:/%';

-- Also clean up any other hardcoded paths that might exist
UPDATE `user_profiles` 
SET `profile_photo_url` = NULL 
WHERE `profile_photo_url` IS NOT NULL 
   AND (`profile_photo_url` NOT LIKE 'http://%' 
   AND `profile_photo_url` NOT LIKE 'https://%' 
   AND `profile_photo_url` NOT LIKE 'file:///%');

-- Commit the changes
COMMIT; 