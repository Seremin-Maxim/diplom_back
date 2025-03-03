CREATE TABLE IF NOT EXISTS lesson_enrollments (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);

-- Индексы для ускорения поиска
CREATE INDEX idx_lesson_enrollments_student_id ON lesson_enrollments(student_id);
CREATE INDEX idx_lesson_enrollments_lesson_id ON lesson_enrollments(lesson_id);

-- Триггер для автоматического обновления updated_at
CREATE TRIGGER update_lesson_enrollments_updated_at
BEFORE UPDATE ON lesson_enrollments
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();