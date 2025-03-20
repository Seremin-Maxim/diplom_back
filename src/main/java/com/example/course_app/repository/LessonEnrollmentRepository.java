package com.example.course_app.repository;

import com.example.course_app.entity.enrollments.LessonEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с записями студентов на уроки.
 */
@Repository
public interface LessonEnrollmentRepository extends JpaRepository<LessonEnrollment, Long> {
    
    /**
     * Найти все записи конкретного студента.
     * 
     * @param studentId идентификатор студента
     * @return список записей студента на уроки
     */
    List<LessonEnrollment> findByStudentId(Long studentId);
    
    /**
     * Найти все записи для конкретного урока.
     * 
     * @param lessonId идентификатор урока
     * @return список записей на урок
     */
    List<LessonEnrollment> findByLessonId(Long lessonId);
    
    /**
     * Найти запись конкретного студента на конкретный урок.
     * 
     * @param studentId идентификатор студента
     * @param lessonId идентификатор урока
     * @return запись студента на урок (если существует)
     */
    Optional<LessonEnrollment> findByStudentIdAndLessonId(Long studentId, Long lessonId);
    
    /**
     * Найти все завершенные записи студента.
     * 
     * @param studentId идентификатор студента
     * @return список завершенных уроков
     */
    List<LessonEnrollment> findByStudentIdAndCompletedTrue(Long studentId);
    
    /**
     * Проверить, существует ли запись студента на урок.
     * 
     * @param studentId идентификатор студента
     * @param lessonId идентификатор урока
     * @return true, если запись существует
     */
    boolean existsByStudentIdAndLessonId(Long studentId, Long lessonId);
}
