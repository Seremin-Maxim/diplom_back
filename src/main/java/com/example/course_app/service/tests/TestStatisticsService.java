package com.example.course_app.service.tests;

import java.util.Map;

/**
 * Сервис для получения статистики по тестам.
 */
public interface TestStatisticsService {
    
    /**
     * Получить количество студентов, прошедших тест.
     * 
     * @param testId идентификатор теста
     * @return количество студентов
     */
    long getStudentCountForTest(Long testId);
    
    /**
     * Получить средний балл за тест.
     * 
     * @param testId идентификатор теста
     * @return средний балл
     */
    double getAverageScoreForTest(Long testId);
    
    /**
     * Получить процент успешного прохождения теста.
     * Успешным считается прохождение с результатом выше порогового значения.
     * 
     * @param testId идентификатор теста
     * @param threshold пороговое значение (в процентах от максимального балла)
     * @return процент успешного прохождения
     */
    double getSuccessRateForTest(Long testId, double threshold);
    
    /**
     * Получить распределение оценок за тест.
     * 
     * @param testId идентификатор теста
     * @return карта с распределением оценок (ключ - диапазон оценки, значение - количество студентов)
     */
    Map<String, Long> getScoreDistributionForTest(Long testId);
    
    /**
     * Получить среднее время прохождения теста (в минутах).
     * 
     * @param testId идентификатор теста
     * @return среднее время прохождения
     */
    double getAverageCompletionTimeForTest(Long testId);
    
    /**
     * Получить количество незавершенных попыток прохождения теста.
     * 
     * @param testId идентификатор теста
     * @return количество незавершенных попыток
     */
    long getIncompleteAttemptsCountForTest(Long testId);
    
    /**
     * Получить статистику по конкретному вопросу в тесте.
     * 
     * @param questionId идентификатор вопроса
     * @return карта с статистикой по вопросу
     */
    Map<String, Object> getQuestionStatistics(Long questionId);
    
    /**
     * Получить общую статистику по тесту.
     * 
     * @param testId идентификатор теста
     * @return карта с общей статистикой
     */
    Map<String, Object> getTestStatistics(Long testId);
}
