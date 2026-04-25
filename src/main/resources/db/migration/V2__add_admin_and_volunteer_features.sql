-- Добавляем поля для волонтёра: дата рождения и рейтинг
ALTER TABLE volunteers ADD COLUMN IF NOT EXISTS birth_date DATE;
ALTER TABLE volunteers ADD COLUMN IF NOT EXISTS rating NUMERIC(3,2) NOT NULL DEFAULT 0.00;
ALTER TABLE volunteers ADD COLUMN IF NOT EXISTS completed_tasks_count INTEGER NOT NULL DEFAULT 0;

-- Дефолтный администратор: login=admin, password=admin (BCrypt)
INSERT INTO users (username, email, password_hash, role, enabled, created_at)
VALUES (
    'admin',
    'admin@platform.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh7y',
    'ADMIN',
    TRUE,
    CURRENT_TIMESTAMP
)
ON CONFLICT (username) DO NOTHING;
