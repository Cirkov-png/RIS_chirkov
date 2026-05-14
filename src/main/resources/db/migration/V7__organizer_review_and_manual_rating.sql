ALTER TABLE applications ADD COLUMN IF NOT EXISTS organizer_rating NUMERIC(5, 2);
ALTER TABLE applications ADD COLUMN IF NOT EXISTS task_completed_successfully BOOLEAN;
ALTER TABLE applications ADD COLUMN IF NOT EXISTS organizer_reviewed_at TIMESTAMP;

ALTER TABLE volunteers ADD COLUMN IF NOT EXISTS manual_rating_sum NUMERIC(12, 4) NOT NULL DEFAULT 0;
ALTER TABLE volunteers ADD COLUMN IF NOT EXISTS manual_rating_count INTEGER NOT NULL DEFAULT 0;
