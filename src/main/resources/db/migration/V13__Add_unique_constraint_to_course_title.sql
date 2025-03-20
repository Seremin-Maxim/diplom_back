-- Добавление ограничения уникальности для поля title в таблице courses
ALTER TABLE courses ADD CONSTRAINT uk_courses_title UNIQUE (title);
