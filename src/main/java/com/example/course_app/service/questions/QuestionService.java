package com.example.course_app.service.questions;

import com.example.course_app.entity.questions.Question;
import com.example.course_app.entity.questions.QuestionType;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с вопросами.
 */
public interface QuestionService {
    
    /**
     * Создать новый вопрос для теста.
     * 
     * @param testId идентификатор теста
     * @param text текст вопроса
     * @param type тип вопроса
     * @param points количество баллов за правильный ответ
     * @return созданный вопрос
     */
    Question createQuestion(Long testId, String text, QuestionType type, Integer points);
    
    /**
     * Получить вопрос по идентификатору.
     * 
     * @param id идентификатор вопроса
     * @return вопрос или пустой Optional, если вопрос не найден
     */
    Optional<Question> getQuestionById(Long id);
    
    /**
     * Получить все вопросы для теста.
     * 
     * @param testId идентификатор теста
     * @return список вопросов теста
     */
    List<Question> getQuestionsByTestId(Long testId);
    
    /**
     * Получить все вопросы определенного типа.
     * 
     * @param type тип вопроса
     * @return список вопросов указанного типа
     */
    List<Question> getQuestionsByType(QuestionType type);
    
    /**
     * Получить все вопросы для теста определенного типа.
     * 
     * @param testId идентификатор теста
     * @param type тип вопроса
     * @return список вопросов теста указанного типа
     */
    List<Question> getQuestionsByTestIdAndType(Long testId, QuestionType type);
    
    /**
     * Обновить информацию о вопросе.
     * 
     * @param id идентификатор вопроса
     * @param text новый текст вопроса
     * @param type новый тип вопроса
     * @param points новое количество баллов
     * @return обновленный вопрос
     */
    Question updateQuestion(Long id, String text, QuestionType type, Integer points);
    
    /**
     * Удалить вопрос.
     * 
     * @param id идентификатор вопроса
     */
    void deleteQuestion(Long id);
    
    /**
     * Проверить, принадлежит ли вопрос тесту.
     * 
     * @param questionId идентификатор вопроса
     * @param testId идентификатор теста
     * @return true, если вопрос принадлежит тесту
     */
    boolean isQuestionBelongsToTest(Long questionId, Long testId);
    
    /**
     * Получить количество правильных ответов для вопроса.
     * 
     * @param questionId идентификатор вопроса
     * @return количество правильных ответов
     */
    long getCorrectAnswersCount(Long questionId);
    
    /**
     * Проверить, существует ли вопрос с указанным идентификатором.
     *
     * @param id идентификатор вопроса
     * @return true, если вопрос существует
     */
    boolean existsById(Long id);
}
