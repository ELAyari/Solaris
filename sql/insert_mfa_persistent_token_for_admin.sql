-- Insert a persistent MFA token for admin user with id = 11 and password 'admin'
-- Replace 'your_device_identifier_here' with your actual device identifier string

INSERT INTO mfa_persistent_tokens (user_id, device_identifier, token, expiry_date)
VALUES (
    11,
    'your_device_identifier_here',
    "123456789", -- generates a unique token string
    DATE_ADD(NOW(), INTERVAL 30 DAY) -- token valid for 30 days from now
);
