package com.example.course_app.repository;

import com.example.course_app.entity.questions.Question;
import com.example.course_app.entity.questions.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с вопросами.
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    /**
     * Найти все вопросы для конкретного теста.
     * 
     * @param testId идентификатор теста
     * @return список вопросов теста
     */
    List<Question> findByTestId(Long testId);
    
    /**
     * Найти все вопросы определенного типа.
     * 
     * @param type тип вопроса
     * @return список вопросов указанного типа
     */
    List<Question> findByType(QuestionType type);
    
    /**
     * Найти все вопросы для конкретного теста определенного типа.
     * 
     * @param testId идентификатор теста
     * @param type тип вопроса
     * @return список вопросов теста указанного типа
     */
    List<Question> findByTestIdAndType(Long testId, QuestionType type);
    
    /**
     * Подсчитать количество вопросов в тесте.
     * 
     * @param testId идентификатор теста
     * @return количество вопросов
     */
    long countByTestId(Long testId);
    
    /**
     * Подсчитать сумму баллов за все вопросы в тесте.
     * 
     * @param testId идентификатор теста
     * @return сумма баллов
     */
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(q.points), 0) FROM Question q WHERE q.test.id = :testId")
    Integer sumPointsByTestId(Long testId);
}
