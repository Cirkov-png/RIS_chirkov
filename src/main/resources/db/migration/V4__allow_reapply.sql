-- Убираем уникальный constraint чтобы разрешить повторные заявки после отзыва
ALTER TABLE applications DROP CONSTRAINT IF EXISTS applications_task_id_volunteer_id_key;

-- Счётчик попыток подачи заявки на конкретную задачу
ALTER TABLE applications ADD COLUMN IF NOT EXISTS attempt_number INTEGER NOT NULL DEFAULT 1;
