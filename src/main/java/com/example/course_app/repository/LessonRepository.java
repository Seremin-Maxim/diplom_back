package com.example.course_app.repository;

import com.example.course_app.entity.lessons.Lesson;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с уроками.
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
    /**
     * Найти урок по ID с загрузкой связанных сущностей.
     * Использует EntityGraph для загрузки связанных сущностей.
     * 
     * @param id идентификатор урока
     * @return урок с загруженными связями
     */
    @EntityGraph(attributePaths = {"course", "course.teacher"})
    Optional<Lesson> findLessonWithRelationsById(Long id);
    
    /**
     * Найти все уроки для конкретного курса.
     * Использует EntityGraph для загрузки связанных сущностей.
     * 
     * @param courseId идентификатор курса
     * @return список уроков курса
     */
    @EntityGraph(attributePaths = {"course", "course.teacher"})
    List<Lesson> findByCourseId(Long courseId);
    
    /**
     * Найти все уроки для конкретного курса, отсортированные по порядковому номеру.
     * Использует EntityGraph для загрузки связанных сущностей.
     * 
     * @param courseId идентификатор курса
     * @return отсортированный список уроков курса
     */
    @EntityGraph(attributePaths = {"course", "course.teacher"})
    List<Lesson> findByCourseIdOrderByOrderNumberAsc(Long courseId);
    
    /**
     * Найти все уроки с контентом для конкретного курса.
     * Использует EntityGraph для загрузки связанных сущностей.
     * 
     * @param courseId идентификатор курса
     * @return список уроков с контентом
     */
    @EntityGraph(attributePaths = {"course"})
    List<Lesson> findByCourseIdAndIsContentTrue(Long courseId);
    
    /**
     * Найти максимальный порядковый номер урока в курсе.
     * 
     * @param courseId идентификатор курса
     * @return максимальный порядковый номер или 0, если уроков нет
     */
    @Query("SELECT COALESCE(MAX(l.orderNumber), 0) FROM Lesson l WHERE l.course.id = ?1")
    Integer findMaxOrderNumberByCourseId(Long courseId);
}
