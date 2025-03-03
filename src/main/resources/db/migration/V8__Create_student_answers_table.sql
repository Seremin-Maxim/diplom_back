CREATE TABLE IF NOT EXISTS student_answers (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT,
    is_correct BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES student_submissions(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Индексы для ускорения поиска
CREATE INDEX idx_student_answers_submission_id ON student_answers(submission_id);
CREATE INDEX idx_student_answers_question_id ON student_answers(question_id);

-- Триггер для автоматического обновления updated_at
CREATE TRIGGER update_student_answers_updated_at
BEFORE UPDATE ON student_answers
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();