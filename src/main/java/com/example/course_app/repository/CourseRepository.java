package com.example.course_app.repository;

import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.courses.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с курсами.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    /**
     * Найти все курсы конкретного преподавателя.
     * 
     * @param teacherId идентификатор преподавателя
     * @return список курсов преподавателя
     */
    List<Course> findByTeacherId(Long teacherId);
    
    /**
     * Найти все опубликованные курсы.
     * 
     * @return список опубликованных курсов
     */
    List<Course> findByStatus(CourseStatus status);
    
    /**
     * Найти все публичные опубликованные курсы.
     * 
     * @return список публичных опубликованных курсов
     */
    List<Course> findByStatusAndIsPublicTrue(CourseStatus status);
    
    /**
     * Найти курсы по названию (частичное совпадение, без учета регистра).
     * 
     * @param title часть названия курса
     * @return список курсов, содержащих указанную строку в названии
     */
    List<Course> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Проверить, существует ли курс с таким названием.
     * 
     * @param title название курса
     * @return true, если курс с таким названием существует
     */
    boolean existsByTitle(String title);
    
    /**
     * Проверить, существует ли курс с таким названием, исключая текущий курс.
     * Это полезно при обновлении курса, чтобы не считать текущий курс как дубликат.
     * 
     * @param title название курса
     * @param id идентификатор текущего курса
     * @return true, если курс с таким названием существует и это не текущий курс
     */
    boolean existsByTitleAndIdNot(String title, Long id);
}
