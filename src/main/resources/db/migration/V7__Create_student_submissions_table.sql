CREATE TABLE IF NOT EXISTS student_submissions (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    test_id BIGINT NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    score INT,
    reviewed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE
);

-- Индексы для ускорения поиска
CREATE INDEX idx_student_submissions_student_id ON student_submissions(student_id);
CREATE INDEX idx_student_submissions_test_id ON student_submissions(test_id);

-- Триггер для автоматического обновления updated_at
CREATE TRIGGER update_student_submissions_updated_at
BEFORE UPDATE ON student_submissions
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();