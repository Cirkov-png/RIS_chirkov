-- Убираем unique constraint чтобы можно было пересчитывать матчинг несколько раз
ALTER TABLE match_results DROP CONSTRAINT IF EXISTS match_results_task_id_volunteer_id_key;
DROP INDEX IF EXISTS idx_match_results_task_score;
CREATE INDEX IF NOT EXISTS idx_match_results_task_score ON match_results (task_id, match_score DESC);
