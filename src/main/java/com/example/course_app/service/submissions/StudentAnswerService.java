package com.example.course_app.service.submissions;

import com.example.course_app.entity.submissions.StudentAnswer;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с ответами студентов на вопросы.
 */
public interface StudentAnswerService {
    
    /**
     * Создать новый ответ студента на вопрос.
     * 
     * @param submissionId идентификатор отправки
     * @param questionId идентификатор вопроса
     * @param answerText текст ответа
     * @return созданный ответ
     */
    StudentAnswer createStudentAnswer(Long submissionId, Long questionId, String answerText);
    
    /**
     * Получить ответ по идентификатору.
     * 
     * @param id идентификатор ответа
     * @return ответ или пустой Optional, если ответ не найден
     */
    Optional<StudentAnswer> getStudentAnswerById(Long id);
    
    /**
     * Получить все ответы для конкретной отправки.
     * 
     * @param submissionId идентификатор отправки
     * @return список ответов
     */
    List<StudentAnswer> getAnswersBySubmissionId(Long submissionId);
    
    /**
     * Получить все ответы на конкретный вопрос.
     * 
     * @param questionId идентификатор вопроса
     * @return список ответов
     */
    List<StudentAnswer> getAnswersByQuestionId(Long questionId);
    
    /**
     * Получить ответ студента на конкретный вопрос в конкретной отправке.
     * 
     * @param submissionId идентификатор отправки
     * @param questionId идентификатор вопроса
     * @return ответ или null, если ответ не найден
     */
    StudentAnswer getAnswerBySubmissionIdAndQuestionId(Long submissionId, Long questionId);
    
    /**
     * Обновить текст ответа.
     * 
     * @param id идентификатор ответа
     * @param answerText новый текст ответа
     * @return обновленный ответ
     */
    StudentAnswer updateAnswerText(Long id, String answerText);
    
    /**
     * Отметить ответ как правильный или неправильный.
     * 
     * @param id идентификатор ответа
     * @param isCorrect флаг правильности
     * @return обновленный ответ
     */
    StudentAnswer markAnswerAsCorrect(Long id, boolean isCorrect);
    
    /**
     * Удалить ответ.
     * 
     * @param id идентификатор ответа
     */
    void deleteStudentAnswer(Long id);
    
    /**
     * Удалить все ответы для конкретной отправки.
     * 
     * @param submissionId идентификатор отправки
     */
    void deleteAllAnswersBySubmissionId(Long submissionId);
    
    /**
     * Проверить ответ студента на правильность.
     * Для вопросов с автоматической проверкой.
     * 
     * @param answerId идентификатор ответа
     * @return true, если ответ правильный
     */
    boolean checkAnswerCorrectness(Long answerId);
    
    /**
     * Создать новый ответ студента с выбранными вариантами ответа.
     * Для вопросов с выбором (одиночным или множественным).
     * 
     * @param submissionId идентификатор отправки
     * @param questionId идентификатор вопроса
     * @param selectedAnswerIds список идентификаторов выбранных вариантов ответа
     * @return созданный ответ
     */
    StudentAnswer createStudentAnswerWithSelectedOptions(Long submissionId, Long questionId, List<Long> selectedAnswerIds);
    
    /**
     * Получить количество правильных ответов в отправке.
     * 
     * @param submissionId идентификатор отправки
     * @return количество правильных ответов
     */
    int getCorrectAnswersCount(Long submissionId);
}
