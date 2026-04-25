-- Синхронизация sequence с максимальными id в таблицах.
-- Нужна, если данные добавлялись вручную: иначе INSERT может падать с duplicate key.
SELECT setval(
    pg_get_serial_sequence('users', 'id'),
    COALESCE((SELECT MAX(id) FROM users), 0) + 1,
    false
);

SELECT setval(
    pg_get_serial_sequence('volunteers', 'id'),
    COALESCE((SELECT MAX(id) FROM volunteers), 0) + 1,
    false
);

SELECT setval(
    pg_get_serial_sequence('categories', 'id'),
    COALESCE((SELECT MAX(id) FROM categories), 0) + 1,
    false
);

SELECT setval(
    pg_get_serial_sequence('skills', 'id'),
    COALESCE((SELECT MAX(id) FROM skills), 0) + 1,
    false
);

SELECT setval(
    pg_get_serial_sequence('tasks', 'id'),
    COALESCE((SELECT MAX(id) FROM tasks), 0) + 1,
    false
);

SELECT setval(
    pg_get_serial_sequence('task_requirements', 'id'),
    COALESCE((SELECT MAX(id) FROM task_requirements), 0) + 1,
    false
);

SELECT setval(
    pg_get_serial_sequence('volunteer_skills', 'id'),
    COALESCE((SELECT MAX(id) FROM volunteer_skills), 0) + 1,
    false
);

SELECT setval(
    pg_get_serial_sequence('match_results', 'id'),
    COALESCE((SELECT MAX(id) FROM match_results), 0) + 1,
    false
);

SELECT setval(
    pg_get_serial_sequence('applications', 'id'),
    COALESCE((SELECT MAX(id) FROM applications), 0) + 1,
    false
);
