package com.example.course_app.service.courses;

import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.courses.CourseStatus;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с курсами.
 */
public interface CourseService {
    
    /**
     * Создать новый курс.
     *
     * @param course данные курса для создания
     * @param teacherId идентификатор преподавателя
     * @return созданный курс
     * @throws IllegalArgumentException если курс с таким названием уже существует
     */
    Course createCourse(Course course, Long teacherId);
    
    /**
     * Обновить существующий курс.
     *
     * @param id идентификатор курса
     * @param courseDetails обновленные данные курса
     * @return обновленный курс
     * @throws IllegalArgumentException если курс с таким названием уже существует
     * @throws IllegalStateException если курс не найден
     */
    Course updateCourse(Long id, Course courseDetails);
    
    /**
     * Получить курс по идентификатору.
     *
     * @param id идентификатор курса
     * @return курс или пустой Optional, если курс не найден
     */
    Optional<Course> getCourseById(Long id);
    
    /**
     * Получить все курсы.
     *
     * @return список всех курсов
     */
    List<Course> getAllCourses();
    
    /**
     * Получить все курсы преподавателя.
     *
     * @param teacherId идентификатор преподавателя
     * @return список курсов преподавателя
     */
    List<Course> getCoursesByTeacherId(Long teacherId);
    
    /**
     * Получить все опубликованные курсы.
     *
     * @return список опубликованных курсов
     */
    List<Course> getPublishedCourses();
    
    /**
     * Получить все публичные опубликованные курсы.
     *
     * @return список публичных опубликованных курсов
     */
    List<Course> getPublicCourses();
    
    /**
     * Поиск курсов по названию (частичное совпадение).
     *
     * @param title часть названия курса
     * @return список курсов, содержащих указанную строку в названии
     */
    List<Course> searchCoursesByTitle(String title);
    
    /**
     * Изменить статус курса.
     *
     * @param id идентификатор курса
     * @param status новый статус курса
     * @return обновленный курс
     * @throws IllegalStateException если курс не найден
     */
    Course changeCourseStatus(Long id, CourseStatus status);
    
    /**
     * Изменить публичность курса.
     *
     * @param id идентификатор курса
     * @param isPublic флаг публичности
     * @return обновленный курс
     * @throws IllegalStateException если курс не найден
     */
    Course changeCoursePublicity(Long id, boolean isPublic);
    
    /**
     * Удалить курс по идентификатору.
     *
     * @param id идентификатор курса
     * @throws IllegalStateException если курс не найден
     */
    void deleteCourse(Long id);
    
    /**
     * Проверить, существует ли курс с указанным идентификатором.
     *
     * @param id идентификатор курса
     * @return true, если курс существует
     */
    boolean existsById(Long id);
    
    /**
     * Проверить, принадлежит ли курс указанному преподавателю.
     *
     * @param courseId идентификатор курса
     * @param teacherId идентификатор преподавателя
     * @return true, если курс принадлежит преподавателю
     */
    boolean isCourseOwnedByTeacher(Long courseId, Long teacherId);
}
