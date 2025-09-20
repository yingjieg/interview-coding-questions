-- Sample data for development - one default user
-- This file is automatically executed by Hibernate after DDL creation

-- Insert default user (password: "password")
INSERT INTO users (id, first_name, last_name, email, password, enabled, email_verified, created_at, updated_at)
VALUES (1, 'Test', 'User', 'test@test.com', '$2a$12$LQv3c1yqBw71UDAOZuxqj.wWOBl0z7.4YcGqVgGLz2F8JJJZK9Jka', true, true,
        now(), now());

-- Reset sequence to continue from imported data
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));