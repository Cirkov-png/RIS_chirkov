-- Публичный профиль пользователя (организатор и др.): ФИО, контакты, о себе, аватар по URL
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_full_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_phone VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_bio TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_avatar_url TEXT;
