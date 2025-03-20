package com.example.course_app.repository;

import com.example.course_app.entity.enrollments.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с записями студентов на курсы.
 */
@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    
    /**
     * Найти все записи конкретного студента.
     * 
     * @param studentId идентификатор студента
     * @return список записей студента на курсы
     */
    List<CourseEnrollment> findByStudentId(Long studentId);
    
    /**
     * Найти все записи для конкретного курса.
     * 
     * @param courseId идентификатор курса
     * @return список записей на курс
     */
    List<CourseEnrollment> findByCourseId(Long courseId);
    
    /**
     * Найти запись конкретного студента на конкретный курс.
     * 
     * @param studentId идентификатор студента
     * @param courseId идентификатор курса
     * @return запись студента на курс (если существует)
     */
    Optional<CourseEnrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    /**
     * Найти все завершенные записи студента.
     * 
     * @param studentId идентификатор студента
     * @return список завершенных курсов
     */
    List<CourseEnrollment> findByStudentIdAndCompletedTrue(Long studentId);
    
    /**
     * Проверить, существует ли запись студента на курс.
     * 
     * @param studentId идентификатор студента
     * @param courseId идентификатор курса
     * @return true, если запись существует
     */
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}
