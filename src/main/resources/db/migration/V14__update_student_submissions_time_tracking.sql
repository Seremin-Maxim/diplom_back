-- Сначала удаляем старое поле
ALTER TABLE student_submissions 
DROP COLUMN submitted_at;

-- Затем добавляем новые поля для отслеживания времени
ALTER TABLE student_submissions
ADD COLUMN start_time TIMESTAMP,
ADD COLUMN end_time TIMESTAMP; 