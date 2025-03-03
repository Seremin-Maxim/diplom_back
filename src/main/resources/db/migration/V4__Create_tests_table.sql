CREATE TABLE IF NOT EXISTS tests (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    requires_manual_check BOOLEAN DEFAULT FALSE,
    is_content BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);

-- Индекс для ускорения поиска по lesson_id
CREATE INDEX idx_tests_lesson_id ON tests(lesson_id);

-- Триггер для автоматического обновления updated_at
CREATE TRIGGER update_tests_updated_at
BEFORE UPDATE ON tests
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();