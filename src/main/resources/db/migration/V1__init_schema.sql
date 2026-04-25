-- Логическая модель (идемпотентно: подходит и для пустой БД, и если таблицы уже созданы вручную)
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

CREATE TABLE IF NOT EXISTS skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category_id BIGINT REFERENCES categories (id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_skills_name_category ON skills (name, COALESCE(category_id, 0));

CREATE TABLE IF NOT EXISTS volunteers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    full_name VARCHAR(255),
    phone VARCHAR(50),
    region VARCHAR(255),
    bio TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS volunteer_skills (
    id BIGSERIAL PRIMARY KEY,
    volunteer_id BIGINT NOT NULL REFERENCES volunteers (id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills (id) ON DELETE CASCADE,
    proficiency_level INTEGER NOT NULL DEFAULT 3,
    CONSTRAINT chk_volunteer_skills_proficiency CHECK (proficiency_level BETWEEN 1 AND 5),
    UNIQUE (volunteer_id, skill_id)
);

CREATE INDEX IF NOT EXISTS idx_volunteer_skills_skill ON volunteer_skills (skill_id);

CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    organizer_id BIGINT NOT NULL REFERENCES users (id),
    category_id BIGINT REFERENCES categories (id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL,
    location VARCHAR(500),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tasks_organizer ON tasks (organizer_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks (status);

CREATE TABLE IF NOT EXISTS task_requirements (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills (id) ON DELETE CASCADE,
    importance_weight NUMERIC(12, 4) NOT NULL DEFAULT 1.0000,
    CONSTRAINT chk_task_requirements_weight CHECK (importance_weight > 0),
    UNIQUE (task_id, skill_id)
);

CREATE INDEX IF NOT EXISTS idx_task_requirements_task ON task_requirements (task_id);

CREATE TABLE IF NOT EXISTS match_results (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    volunteer_id BIGINT NOT NULL REFERENCES volunteers (id) ON DELETE CASCADE,
    match_score NUMERIC(14, 6) NOT NULL,
    computed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (task_id, volunteer_id)
);

CREATE INDEX IF NOT EXISTS idx_match_results_task_score ON match_results (task_id, match_score DESC);

CREATE TABLE IF NOT EXISTS applications (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    volunteer_id BIGINT NOT NULL REFERENCES volunteers (id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    message TEXT,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (task_id, volunteer_id)
);

CREATE INDEX IF NOT EXISTS idx_applications_task ON applications (task_id);
CREATE INDEX IF NOT EXISTS idx_applications_volunteer ON applications (volunteer_id);
