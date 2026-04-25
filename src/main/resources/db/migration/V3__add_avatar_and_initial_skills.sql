-- Аватарка волонтёра (Base64 или URL)
ALTER TABLE volunteers ADD COLUMN IF NOT EXISTS avatar_url TEXT;

-- Начальные категории навыков
INSERT INTO categories (name, description) VALUES
    ('IT', 'Информационные технологии'),
    ('Медицина', 'Медицинская помощь и уход'),
    ('Образование', 'Обучение и репетиторство'),
    ('Строительство', 'Строительные и ремонтные работы'),
    ('Транспорт', 'Водители и логистика'),
    ('Экология', 'Охрана окружающей среды')
ON CONFLICT (name) DO NOTHING;

-- Начальные навыки
INSERT INTO skills (name, category_id) VALUES
    ('Программирование', (SELECT id FROM categories WHERE name = 'IT')),
    ('Веб-разработка', (SELECT id FROM categories WHERE name = 'IT')),
    ('Администрирование систем', (SELECT id FROM categories WHERE name = 'IT')),
    ('Первая помощь', (SELECT id FROM categories WHERE name = 'Медицина')),
    ('Уход за пожилыми', (SELECT id FROM categories WHERE name = 'Медицина')),
    ('Психологическая поддержка', (SELECT id FROM categories WHERE name = 'Медицина')),
    ('Репетиторство', (SELECT id FROM categories WHERE name = 'Образование')),
    ('Организация мероприятий', (SELECT id FROM categories WHERE name = 'Образование')),
    ('Перевод и языки', (SELECT id FROM categories WHERE name = 'Образование')),
    ('Малярные работы', (SELECT id FROM categories WHERE name = 'Строительство')),
    ('Сантехника', (SELECT id FROM categories WHERE name = 'Строительство')),
    ('Вождение', (SELECT id FROM categories WHERE name = 'Транспорт')),
    ('Логистика', (SELECT id FROM categories WHERE name = 'Транспорт')),
    ('Озеленение', (SELECT id FROM categories WHERE name = 'Экология')),
    ('Уборка территорий', (SELECT id FROM categories WHERE name = 'Экология'))
ON CONFLICT DO NOTHING;
