package com.example.course_app.service.answers;

import com.example.course_app.entity.answers.Answer;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с ответами на вопросы.
 */
public interface AnswerService {
    
    /**
     * Создать новый вариант ответа для вопроса.
     * 
     * @param questionId идентификатор вопроса
     * @param text текст ответа
     * @param isCorrect флаг, указывающий, является ли ответ правильным
     * @return созданный ответ
     */
    Answer createAnswer(Long questionId, String text, boolean isCorrect);
    
    /**
     * Получить ответ по идентификатору.
     * 
     * @param id идентификатор ответа
     * @return ответ или пустой Optional, если ответ не найден
     */
    Optional<Answer> getAnswerById(Long id);
    
    /**
     * Получить все ответы для вопроса.
     * 
     * @param questionId идентификатор вопроса
     * @return список ответов для вопроса
     */
    List<Answer> getAnswersByQuestionId(Long questionId);
    
    /**
     * Получить все правильные ответы для вопроса.
     * 
     * @param questionId идентификатор вопроса
     * @return список правильных ответов для вопроса
     */
    List<Answer> getCorrectAnswersByQuestionId(Long questionId);
    
    /**
     * Обновить информацию об ответе.
     * 
     * @param id идентификатор ответа
     * @param text новый текст ответа
     * @param isCorrect новый флаг правильности
     * @return обновленный ответ
     */
    Answer updateAnswer(Long id, String text, boolean isCorrect);
    
    /**
     * Удалить ответ.
     * 
     * @param id идентификатор ответа
     */
    void deleteAnswer(Long id);
    
    /**
     * Проверить, принадлежит ли ответ вопросу.
     * 
     * @param answerId идентификатор ответа
     * @param questionId идентификатор вопроса
     * @return true, если ответ принадлежит вопросу
     */
    boolean isAnswerBelongsToQuestion(Long answerId, Long questionId);
    
    /**
     * Проверить, является ли ответ правильным.
     * 
     * @param answerId идентификатор ответа
     * @return true, если ответ правильный
     */
    boolean isAnswerCorrect(Long answerId);
    
    /**
     * Создать набор ответов для вопроса с одним правильным ответом.
     * 
     * @param questionId идентификатор вопроса
     * @param correctAnswerText текст правильного ответа
     * @param incorrectAnswersText тексты неправильных ответов
     * @return список созданных ответов
     */
    List<Answer> createSingleChoiceAnswers(Long questionId, String correctAnswerText, List<String> incorrectAnswersText);
    
    /**
     * Создать набор ответов для вопроса с несколькими правильными ответами.
     * 
     * @param questionId идентификатор вопроса
     * @param correctAnswersText тексты правильных ответов
     * @param incorrectAnswersText тексты неправильных ответов
     * @return список созданных ответов
     */
    List<Answer> createMultipleChoiceAnswers(Long questionId, List<String> correctAnswersText, List<String> incorrectAnswersText);
}
