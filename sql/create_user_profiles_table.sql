-- Create user_profiles table
CREATE TABLE IF NOT EXISTS user_profiles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    profile_photo_url VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    description TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Add indexes for better performance (only if they don't exist)
SELECT IF(
    NOT EXISTS(
        SELECT 1 FROM information_schema.statistics 
        WHERE table_schema = DATABASE()
        AND table_name = 'user_profiles' 
        AND index_name = 'idx_user_profiles_user_id'
    ),
    'CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id)',
    'SELECT "Index already exists"'
) INTO @sql;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add comment to the table
COMMENT ON TABLE user_profiles IS 'Additional user profile information';

-- Add comments to columns
ALTER TABLE user_profiles 
    MODIFY COLUMN id INT AUTO_INCREMENT COMMENT 'Primary key',
    MODIFY COLUMN user_id INT NOT NULL COMMENT 'Foreign key reference to users table',
    MODIFY COLUMN profile_photo_url VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'URL or path to user profile photo',
    MODIFY COLUMN description TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'User profile description',
    MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record last update timestamp'; 