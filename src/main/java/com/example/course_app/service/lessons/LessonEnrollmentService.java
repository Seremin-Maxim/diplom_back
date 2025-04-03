package com.example.course_app.service.lessons;

import com.example.course_app.entity.enrollments.LessonEnrollment;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления зачислением студентов на уроки.
 */
public interface LessonEnrollmentService {
    
    /**
     * Зачислить студента на урок.
     *
     * @param studentId идентификатор студента
     * @param lessonId идентификатор урока
     * @return информация о зачислении
     * @throws IllegalArgumentException если студент или урок не найдены
     * @throws IllegalStateException если студент уже зачислен на урок или не зачислен на курс
     */
    LessonEnrollment enrollStudentToLesson(Long studentId, Long lessonId);
    
    /**
     * Отметить урок как завершенный для студента.
     *
     * @param studentId идентификатор студента
     * @param lessonId идентификатор урока
     * @return обновленная информация о зачислении
     * @throws IllegalStateException если зачисление не найдено
     */
    LessonEnrollment completeLessonForStudent(Long studentId, Long lessonId);
    
    /**
     * Получить информацию о зачислении по идентификатору.
     *
     * @param enrollmentId идентификатор зачисления
     * @return информация о зачислении или пустой Optional, если зачисление не найдено
     */
    Optional<LessonEnrollment> getEnrollmentById(Long enrollmentId);
    
    /**
     * Получить все зачисления студента на уроки.
     *
     * @param studentId идентификатор студента
     * @return список зачислений студента
     */
    List<LessonEnrollment> getEnrollmentsByStudentId(Long studentId);
    
    /**
     * Получить все зачисления на урок.
     *
     * @param lessonId идентификатор урока
     * @return список зачислений на урок
     */
    List<LessonEnrollment> getEnrollmentsByLessonId(Long lessonId);
    
    /**
     * Получить все завершенные уроки студента.
     *
     * @param studentId идентификатор студента
     * @return список завершенных уроков
     */
    List<LessonEnrollment> getCompletedLessonsByStudentId(Long studentId);
    
    /**
     * Проверить, зачислен ли студент на урок.
     *
     * @param studentId идентификатор студента
     * @param lessonId идентификатор урока
     * @return true, если студент зачислен на урок
     */
    boolean isStudentEnrolledInLesson(Long studentId, Long lessonId);
    
    /**
     * Проверить, завершил ли студент урок.
     *
     * @param studentId идентификатор студента
     * @param lessonId идентификатор урока
     * @return true, если студент завершил урок
     */
    boolean hasStudentCompletedLesson(Long studentId, Long lessonId);
    
    /**
     * Получить прогресс студента по курсу (процент завершенных уроков).
     *
     * @param studentId идентификатор студента
     * @param courseId идентификатор курса
     * @return процент завершенных уроков (от 0 до 100)
     */
    double getStudentProgressInCourse(Long studentId, Long courseId);
    
    /**
     * Проверить, зачислен ли студент на курс.
     *
     * @param studentId идентификатор студента
     * @param courseId идентификатор курса
     * @return true, если студент зачислен на курс
     */
    boolean isStudentEnrolledInCourse(Long studentId, Long courseId);
}
