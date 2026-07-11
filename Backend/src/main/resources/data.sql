
INSERT INTO users (email, password, role, username, created_at)
SELECT
    'reviewer@gemmaforge.com',
    '$2a$10$7PaDiC966SggeBMQA9yqxeWSM4Lt7ZP86vcya28ccNB3/Zgr7w/mm',
    'REVIEWER',
    'REVIEWER',
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'reviewer@gemmaforge.com'
);
