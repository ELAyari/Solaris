-- SQL script to create sms_verification_tokens table similar to email_verification_tokens

CREATE TABLE IF NOT EXISTS sms_verification_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(191) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_sms
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);
