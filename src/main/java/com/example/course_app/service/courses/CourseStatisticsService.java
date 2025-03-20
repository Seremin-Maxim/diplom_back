package com.example.course_app.service.courses;

import java.util.Map;

/**
 * Сервис для получения статистики по курсам.
 */
public interface CourseStatisticsService {
    
    /**
     * Получить количество студентов, зачисленных на курс.
     *
     * @param courseId идентификатор курса
     * @return количество студентов
     */
    long getStudentCountForCourse(Long courseId);
    
    /**
     * Получить количество уроков в курсе.
     *
     * @param courseId идентификатор курса
     * @return количество уроков
     */
    long getLessonCountForCourse(Long courseId);
    
    /**
     * Получить количество тестов в курсе.
     *
     * @param courseId идентификатор курса
     * @return количество тестов
     */
    long getTestCountForCourse(Long courseId);
    
    /**
     * Получить общую статистику по курсу.
     *
     * @param courseId идентификатор курса
     * @return карта с ключами "studentCount", "lessonCount", "testCount"
     */
    Map<String, Long> getCourseStatistics(Long courseId);
    
    /**
     * Получить количество курсов для преподавателя.
     *
     * @param teacherId идентификатор преподавателя
     * @return количество курсов
     */
    long getCourseCountForTeacher(Long teacherId);
    
    /**
     * Получить количество студентов для всех курсов преподавателя.
     *
     * @param teacherId идентификатор преподавателя
     * @return количество студентов
     */
    long getTotalStudentCountForTeacher(Long teacherId);
    
    /**
     * Получить статистику по всем курсам преподавателя.
     *
     * @param teacherId идентификатор преподавателя
     * @return карта с ключами "courseCount", "studentCount", "lessonCount", "testCount"
     */
    Map<String, Long> getTeacherStatistics(Long teacherId);
}
