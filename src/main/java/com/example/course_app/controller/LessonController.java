package com.example.course_app.controller;

import com.example.course_app.entity.User;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.service.courses.CourseService;
import com.example.course_app.service.lessons.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер для работы с уроками.
 */
@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "http://localhost:3000")
public class LessonController {

    private final LessonService lessonService;
    private final CourseService courseService;

    @Autowired
    public LessonController(LessonService lessonService, CourseService courseService) {
        this.lessonService = lessonService;
        this.courseService = courseService;
    }

    /**
     * Получить все уроки курса.
     *
     * @param courseId идентификатор курса
     * @return список уроков курса
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Lesson>> getLessonsByCourseId(@PathVariable Long courseId) {
        List<Lesson> lessons = lessonService.getLessonsByCourseIdOrdered(courseId);
        return ResponseEntity.ok(lessons);
    }

    /**
     * Получить все уроки с контентом для курса.
     *
     * @param courseId идентификатор курса
     * @return список уроков с контентом
     */
    @GetMapping("/course/{courseId}/content")
    public ResponseEntity<List<Lesson>> getContentLessonsByCourseId(@PathVariable Long courseId) {
        List<Lesson> lessons = lessonService.getContentLessonsByCourseId(courseId);
        return ResponseEntity.ok(lessons);
    }

    /**
     * Получить урок по идентификатору.
     *
     * @param id идентификатор урока
     * @return урок или статус 404, если урок не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable Long id) {
        Optional<Lesson> lesson = lessonService.getLessonById(id);
        return lesson.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создать новый урок в курсе.
     *
     * @param lesson данные урока для создания
     * @param courseId идентификатор курса
     * @param authentication данные аутентификации пользователя
     * @return созданный урок или статус 403, если пользователь не имеет прав
     */
    @PostMapping("/course/{courseId}")
    public ResponseEntity<Lesson> createLesson(@RequestBody Lesson lesson, 
                                              @PathVariable Long courseId,
                                              Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Проверяем, принадлежит ли курс преподавателю
        if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Lesson createdLesson = lessonService.createLesson(lesson, courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLesson);
    }

    /**
     * Обновить существующий урок.
     *
     * @param id идентификатор урока
     * @param lessonDetails обновленные данные урока
     * @param authentication данные аутентификации пользователя
     * @return обновленный урок или статус 403, если пользователь не имеет прав
     */
    @PutMapping("/{id}")
    public ResponseEntity<Lesson> updateLesson(@PathVariable Long id, 
                                              @RequestBody Lesson lessonDetails,
                                              Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Получаем информацию о уроке
        Optional<Lesson> lessonOpt = lessonService.getLessonById(id);
        if (lessonOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Lesson lesson = lessonOpt.get();
        Long courseId = lesson.getCourse().getId();
        
        // Проверяем, принадлежит ли курс преподавателю
        if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Lesson updatedLesson = lessonService.updateLesson(id, lessonDetails);
        return ResponseEntity.ok(updatedLesson);
    }

    /**
     * Изменить порядок урока в курсе.
     *
     * @param id идентификатор урока
     * @param newOrderNumber новый порядковый номер
     * @param authentication данные аутентификации пользователя
     * @return обновленный урок или статус 403, если пользователь не имеет прав
     */
    @PatchMapping("/{id}/order")
    public ResponseEntity<Lesson> changeLessonOrder(@PathVariable Long id, 
                                                  @RequestBody Integer newOrderNumber,
                                                  Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Получаем информацию о уроке
        Optional<Lesson> lessonOpt = lessonService.getLessonById(id);
        if (lessonOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Lesson lesson = lessonOpt.get();
        Long courseId = lesson.getCourse().getId();
        
        // Проверяем, принадлежит ли курс преподавателю
        if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Lesson updatedLesson = lessonService.changeLessonOrder(id, newOrderNumber);
        return ResponseEntity.ok(updatedLesson);
    }

    /**
     * Изменить флаг контента урока.
     *
     * @param id идентификатор урока
     * @param isContent флаг контента
     * @param authentication данные аутентификации пользователя
     * @return обновленный урок или статус 403, если пользователь не имеет прав
     */
    @PatchMapping("/{id}/content-flag")
    public ResponseEntity<Lesson> changeLessonContentFlag(@PathVariable Long id, 
                                                        @RequestBody Boolean isContent,
                                                        Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Получаем информацию о уроке
        Optional<Lesson> lessonOpt = lessonService.getLessonById(id);
        if (lessonOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Lesson lesson = lessonOpt.get();
        Long courseId = lesson.getCourse().getId();
        
        // Проверяем, принадлежит ли курс преподавателю
        if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Lesson updatedLesson = lessonService.changeLessonContentFlag(id, isContent);
        return ResponseEntity.ok(updatedLesson);
    }

    /**
     * Удалить урок по идентификатору.
     *
     * @param id идентификатор урока
     * @param authentication данные аутентификации пользователя
     * @return статус 204 (No Content) или статус 403, если пользователь не имеет прав
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Получаем информацию о уроке
        Optional<Lesson> lessonOpt = lessonService.getLessonById(id);
        if (lessonOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Lesson lesson = lessonOpt.get();
        Long courseId = lesson.getCourse().getId();
        
        // Проверяем, принадлежит ли курс преподавателю
        if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Получить ID пользователя из объекта аутентификации
     * 
     * @param authentication объект аутентификации
     * @return ID пользователя или null, если аутентификация не настроена
     */
    protected Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            return user.getId();
        } catch (Exception e) {
            return null;
        }
    }
}
