-- Add description column to users table
ALTER TABLE users ADD COLUMN description TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- Update existing users to have empty description
UPDATE users SET description = '' WHERE description IS NULL;

-- Add comment to the column
COMMENT ON COLUMN users.description IS 'User profile description'; 