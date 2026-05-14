-- Включаем расширение UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Добавляем uuid колонки ко всем таблицам
ALTER TABLE users          ADD COLUMN IF NOT EXISTS uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE;
ALTER TABLE volunteers     ADD COLUMN IF NOT EXISTS uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE;
ALTER TABLE tasks          ADD COLUMN IF NOT EXISTS uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE;
ALTER TABLE skills         ADD COLUMN IF NOT EXISTS uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE;
ALTER TABLE categories     ADD COLUMN IF NOT EXISTS uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE;
ALTER TABLE applications   ADD COLUMN IF NOT EXISTS uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE;
ALTER TABLE volunteer_skills ADD COLUMN IF NOT EXISTS uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE;
ALTER TABLE task_requirements ADD COLUMN IF NOT EXISTS uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE;
ALTER TABLE match_results   ADD COLUMN IF NOT EXISTS uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE;

-- Таблица для отслеживания задач волонтёром (watchlist)
CREATE TABLE IF NOT EXISTS task_watches (
    id        BIGSERIAL PRIMARY KEY,
    uuid      UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    volunteer_id BIGINT NOT NULL REFERENCES volunteers(id) ON DELETE CASCADE,
    task_id   BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    watched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notified_deadline BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(volunteer_id, task_id)
);

CREATE INDEX IF NOT EXISTS idx_task_watches_volunteer ON task_watches(volunteer_id);
CREATE INDEX IF NOT EXISTS idx_task_watches_task ON task_watches(task_id);

-- Добавляем поля для отслеживания дедлайнов в tasks
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS deadline_notified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS reminder_days INTEGER NOT NULL DEFAULT 3;

-- Добавляем профильные поля в users
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_full_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_phone VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_bio TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_avatar_url TEXT;

-- Индексы на uuid для быстрого поиска по публичному идентификатору
CREATE INDEX IF NOT EXISTS idx_users_uuid ON users(uuid);
CREATE INDEX IF NOT EXISTS idx_volunteers_uuid ON volunteers(uuid);
CREATE INDEX IF NOT EXISTS idx_tasks_uuid ON tasks(uuid);
