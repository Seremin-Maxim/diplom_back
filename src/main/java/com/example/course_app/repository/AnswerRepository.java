package com.example.course_app.repository;

import com.example.course_app.entity.answers.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с ответами на вопросы.
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    
    /**
     * Найти все ответы для конкретного вопроса.
     * 
     * @param questionId идентификатор вопроса
     * @return список ответов для вопроса
     */
    List<Answer> findByQuestionId(Long questionId);
    
    /**
     * Найти все правильные ответы для конкретного вопроса.
     * 
     * @param questionId идентификатор вопроса
     * @return список правильных ответов для вопроса
     */
    List<Answer> findByQuestionIdAndIsCorrectTrue(Long questionId);
}
