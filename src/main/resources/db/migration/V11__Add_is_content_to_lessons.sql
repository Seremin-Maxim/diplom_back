-- Добавление поля is_content в таблицу lessons
ALTER TABLE lessons ADD COLUMN is_content BOOLEAN DEFAULT FALSE;
