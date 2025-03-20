package com.example.course_app.service.lessons;

import java.util.Map;

/**
 * Сервис для получения статистики по урокам.
 */
public interface LessonStatisticsService {
    
    /**
     * Получить количество студентов, зачисленных на урок.
     *
     * @param lessonId идентификатор урока
     * @return количество студентов
     */
    long getStudentCountForLesson(Long lessonId);
    
    /**
     * Получить количество тестов в уроке.
     *
     * @param lessonId идентификатор урока
     * @return количество тестов
     */
    long getTestCountForLesson(Long lessonId);
    
    /**
     * Получить количество студентов, завершивших урок.
     *
     * @param lessonId идентификатор урока
     * @return количество студентов
     */
    long getCompletedStudentCountForLesson(Long lessonId);
    
    /**
     * Получить процент завершения урока студентами.
     *
     * @param lessonId идентификатор урока
     * @return процент завершения (от 0 до 100)
     */
    double getCompletionRateForLesson(Long lessonId);
    
    /**
     * Получить общую статистику по уроку.
     *
     * @param lessonId идентификатор урока
     * @return карта с ключами "studentCount", "testCount", "completedCount", "completionRate"
     */
    Map<String, Object> getLessonStatistics(Long lessonId);
    
    /**
     * Получить статистику по всем урокам курса.
     *
     * @param courseId идентификатор курса
     * @return карта, где ключ - идентификатор урока, значение - статистика урока
     */
    Map<Long, Map<String, Object>> getLessonStatisticsForCourse(Long courseId);
    
    /**
     * Получить средний процент завершения уроков в курсе.
     *
     * @param courseId идентификатор курса
     * @return средний процент завершения (от 0 до 100)
     */
    double getAverageCompletionRateForCourse(Long courseId);
}
