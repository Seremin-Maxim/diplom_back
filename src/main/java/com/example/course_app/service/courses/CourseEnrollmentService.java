package com.example.course_app.service.courses;

import com.example.course_app.entity.enrollments.CourseEnrollment;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления зачислением студентов на курсы.
 */
public interface CourseEnrollmentService {
    
    /**
     * Зачислить студента на курс.
     *
     * @param studentId идентификатор студента
     * @param courseId идентификатор курса
     * @return информация о зачислении
     * @throws IllegalArgumentException если студент или курс не найдены
     * @throws IllegalStateException если студент уже зачислен на курс
     */
    CourseEnrollment enrollStudentToCourse(Long studentId, Long courseId);
    
    /**
     * Отчислить студента с курса.
     *
     * @param studentId идентификатор студента
     * @param courseId идентификатор курса
     * @throws IllegalStateException если зачисление не найдено
     */
    void unenrollStudentFromCourse(Long studentId, Long courseId);
    
    /**
     * Получить информацию о зачислении по идентификатору.
     *
     * @param enrollmentId идентификатор зачисления
     * @return информация о зачислении или пустой Optional, если зачисление не найдено
     */
    Optional<CourseEnrollment> getEnrollmentById(Long enrollmentId);
    
    /**
     * Получить все зачисления студента.
     *
     * @param studentId идентификатор студента
     * @return список зачислений студента
     */
    List<CourseEnrollment> getEnrollmentsByStudentId(Long studentId);
    
    /**
     * Получить все зачисления на курс.
     *
     * @param courseId идентификатор курса
     * @return список зачислений на курс
     */
    List<CourseEnrollment> getEnrollmentsByCourseId(Long courseId);
    
    /**
     * Проверить, зачислен ли студент на курс.
     *
     * @param studentId идентификатор студента
     * @param courseId идентификатор курса
     * @return true, если студент зачислен на курс
     */
    boolean isStudentEnrolledInCourse(Long studentId, Long courseId);
    
    /**
     * Получить количество студентов, зачисленных на курс.
     *
     * @param courseId идентификатор курса
     * @return количество студентов
     */
    long getEnrollmentCountForCourse(Long courseId);
}
