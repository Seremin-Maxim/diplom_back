CREATE TABLE IF NOT EXISTS answers (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Индекс для ускорения поиска по question_id
CREATE INDEX idx_answers_question_id ON answers(question_id);

-- Триггер для автоматического обновления updated_at
CREATE TRIGGER update_answers_updated_at
BEFORE UPDATE ON answers
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();