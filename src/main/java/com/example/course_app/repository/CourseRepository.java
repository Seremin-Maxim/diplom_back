package com.example.course_app.repository;

import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.courses.CourseStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с курсами.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    /**
     * Найти все курсы конкретного преподавателя.
     * Использует EntityGraph для загрузки связанных сущностей.
     * 
     * @param teacherId идентификатор преподавателя
     * @return список курсов преподавателя
     */
    @EntityGraph(attributePaths = {"teacher"})
    List<Course> findByTeacherId(Long teacherId);
    
    /**
     * Найти все опубликованные курсы.
     * 
     * @return список опубликованных курсов
     */
    @EntityGraph(attributePaths = {"teacher"})
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
     * Найти курсы по названию и статусу (частичное совпадение, без учета регистра).
     * 
     * @param title часть названия курса
     * @param status статус курса
     * @return список курсов, содержащих указанную строку в названии и имеющих указанный статус
     */
    @EntityGraph(attributePaths = {"teacher"})
    List<Course> findByTitleContainingIgnoreCaseAndStatus(String title, CourseStatus status);
    
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
    
    /**
     * Найти курс по ID с загрузкой преподавателя.
     * Использует EntityGraph для загрузки связанных сущностей.
     * 
     * @param id идентификатор курса
     * @return курс с загруженными связями
     */
    @EntityGraph(attributePaths = {"teacher"})
    Optional<Course> findCourseWithTeacherById(Long id);
}
