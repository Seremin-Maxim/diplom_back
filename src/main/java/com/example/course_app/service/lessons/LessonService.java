package com.example.course_app.service.lessons;

import com.example.course_app.entity.lessons.Lesson;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с уроками.
 */
public interface LessonService {
    
    /**
     * Создать новый урок в курсе.
     *
     * @param lesson данные урока для создания
     * @param courseId идентификатор курса
     * @return созданный урок
     * @throws IllegalArgumentException если курс не найден
     */
    Lesson createLesson(Lesson lesson, Long courseId);
    
    /**
     * Обновить существующий урок.
     *
     * @param id идентификатор урока
     * @param lessonDetails обновленные данные урока
     * @return обновленный урок
     * @throws IllegalStateException если урок не найден
     */
    Lesson updateLesson(Long id, Lesson lessonDetails);
    
    /**
     * Получить урок по идентификатору.
     *
     * @param id идентификатор урока
     * @return урок или пустой Optional, если урок не найден
     */
    Optional<Lesson> getLessonById(Long id);
    
    /**
     * Получить все уроки курса.
     *
     * @param courseId идентификатор курса
     * @return список уроков курса
     */
    List<Lesson> getLessonsByCourseId(Long courseId);
    
    /**
     * Получить все уроки курса, отсортированные по порядковому номеру.
     *
     * @param courseId идентификатор курса
     * @return отсортированный список уроков курса
     */
    List<Lesson> getLessonsByCourseIdOrdered(Long courseId);
    
    /**
     * Получить все уроки с контентом для курса.
     *
     * @param courseId идентификатор курса
     * @return список уроков с контентом
     */
    List<Lesson> getContentLessonsByCourseId(Long courseId);
    
    /**
     * Изменить порядок урока в курсе.
     *
     * @param id идентификатор урока
     * @param newOrderNumber новый порядковый номер
     * @return обновленный урок
     * @throws IllegalStateException если урок не найден
     */
    Lesson changeLessonOrder(Long id, Integer newOrderNumber);
    
    /**
     * Изменить флаг контента урока.
     *
     * @param id идентификатор урока
     * @param isContent флаг контента
     * @return обновленный урок
     * @throws IllegalStateException если урок не найден
     */
    Lesson changeLessonContentFlag(Long id, boolean isContent);
    
    /**
     * Удалить урок по идентификатору.
     *
     * @param id идентификатор урока
     * @throws IllegalStateException если урок не найден
     */
    void deleteLesson(Long id);
    
    /**
     * Проверить, существует ли урок с указанным идентификатором.
     *
     * @param id идентификатор урока
     * @return true, если урок существует
     */
    boolean existsById(Long id);
    
    /**
     * Проверить, принадлежит ли урок указанному курсу.
     *
     * @param lessonId идентификатор урока
     * @param courseId идентификатор курса
     * @return true, если урок принадлежит курсу
     */
    boolean isLessonBelongsToCourse(Long lessonId, Long courseId);
    
    /**
     * Получить следующий порядковый номер для нового урока в курсе.
     *
     * @param courseId идентификатор курса
     * @return следующий порядковый номер
     */
    Integer getNextOrderNumber(Long courseId);
}
