package com.example.course_app.repository;

import com.example.course_app.entity.submissions.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с ответами студентов на вопросы.
 */
@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    
    /**
     * Найти все ответы для конкретной отправки.
     * 
     * @param submissionId идентификатор отправки
     * @return список ответов студента
     */
    List<StudentAnswer> findBySubmissionId(Long submissionId);
    
    /**
     * Найти все ответы на конкретный вопрос.
     * 
     * @param questionId идентификатор вопроса
     * @return список ответов студентов на вопрос
     */
    List<StudentAnswer> findByQuestionId(Long questionId);
    
    /**
     * Найти ответ студента на конкретный вопрос в конкретной отправке.
     * 
     * @param submissionId идентификатор отправки
     * @param questionId идентификатор вопроса
     * @return ответ студента
     */
    StudentAnswer findBySubmissionIdAndQuestionId(Long submissionId, Long questionId);
    
    /**
     * Найти все правильные ответы для конкретной отправки.
     * 
     * @param submissionId идентификатор отправки
     * @return список правильных ответов
     */
    List<StudentAnswer> findBySubmissionIdAndIsCorrectTrue(Long submissionId);

    /**
     * Найти средний балл для конкретного вопроса
     */
    @Query("SELECT AVG(CAST(a.score AS double)) FROM StudentAnswer a WHERE a.question.id = :questionId AND a.score IS NOT NULL")
    Double findAverageScoreForQuestion(@Param("questionId") Long questionId);
}
