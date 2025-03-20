package com.example.course_app.controller;

import com.example.course_app.entity.tests.Test;
import com.example.course_app.entity.tests.TestType;
import com.example.course_app.entity.User;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.dto.TestDTO;
import com.example.course_app.dto.TestMapper;
import com.example.course_app.service.courses.CourseService;
import com.example.course_app.service.lessons.LessonService;
import com.example.course_app.service.tests.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер для работы с тестами.
 */
@RestController
@RequestMapping("/api/tests")
public class TestController {

    private final TestService testService;
    private final LessonService lessonService;
    private final CourseService courseService;

    @Autowired
    public TestController(TestService testService, LessonService lessonService, CourseService courseService) {
        this.testService = testService;
        this.lessonService = lessonService;
        this.courseService = courseService;
    }

    /**
     * Получить тест по ID.
     *
     * @param id ID теста
     * @return тест или статус 404, если тест не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<TestDTO> getTestById(@PathVariable Long id) {
        Optional<Test> testOptional = testService.getTestById(id);
        return testOptional.map(test -> ResponseEntity.ok(TestMapper.toDTO(test)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Получить все тесты для урока.
     *
     * @param lessonId ID урока
     * @return список тестов
     */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<TestDTO>> getTestsByLessonId(@PathVariable Long lessonId) {
        List<Test> tests = testService.getTestsByLessonId(lessonId);
        return ResponseEntity.ok(TestMapper.toDTOList(tests));
    }

    /**
     * Получить все тесты определенного типа для урока.
     *
     * @param lessonId ID урока
     * @param type тип теста
     * @return список тестов
     */
    @GetMapping("/lesson/{lessonId}/type/{type}")
    public ResponseEntity<List<TestDTO>> getTestsByLessonIdAndType(
            @PathVariable Long lessonId,
            @PathVariable TestType type) {
        List<Test> tests = testService.getTestsByLessonIdAndType(lessonId, type);
        return ResponseEntity.ok(TestMapper.toDTOList(tests));
    }

    /**
     * Создать новый тест для урока.
     *
     * @param lessonId ID урока
     * @param test тест для создания
     * @param authentication объект аутентификации
     * @return созданный тест и статус 201
     */
    @PostMapping("/lesson/{lessonId}")
    public ResponseEntity<TestDTO> createTest(
            @PathVariable Long lessonId,
            @RequestBody Test test,
            Authentication authentication) {
        
        // Получаем ID пользователя из аутентификации
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Получаем ID курса для урока
        Optional<Lesson> lessonOptional = lessonService.getLessonById(lessonId);
        if (lessonOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Long courseId = lessonOptional.get().getCourse().getId();
        
        // Проверяем, принадлежит ли курс преподавателю
        if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Создаем тест
        Test createdTest = testService.createTest(
                lessonId,
                test.getTitle(),
                test.getType(),
                test.isRequiresManualCheck());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(TestMapper.toDTO(createdTest));
    }

    /**
     * Обновить существующий тест.
     *
     * @param id ID теста
     * @param test тест с обновленными данными
     * @param authentication объект аутентификации
     * @return обновленный тест
     */
    @PutMapping("/{id}")
    public ResponseEntity<TestDTO> updateTest(
            @PathVariable Long id,
            @RequestBody Test test,
            Authentication authentication) {
        
        // Получаем ID пользователя из аутентификации
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Получаем существующий тест
        Optional<Test> existingTestOptional = testService.getTestById(id);
        if (existingTestOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Test existingTest = existingTestOptional.get();
        Long lessonId = existingTest.getLesson().getId();
        
        // Получаем ID курса для урока
        Optional<Lesson> lessonOptional = lessonService.getLessonById(lessonId);
        if (lessonOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Long courseId = lessonOptional.get().getCourse().getId();
        
        // Проверяем, принадлежит ли курс преподавателю
        if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Обновляем тест
        Test updatedTest = testService.updateTest(
                id,
                test.getTitle(),
                test.getType(),
                test.isRequiresManualCheck());
        
        return ResponseEntity.ok(TestMapper.toDTO(updatedTest));
    }

    /**
     * Удалить тест.
     *
     * @param id ID теста
     * @param authentication объект аутентификации
     * @return статус 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTest(
            @PathVariable Long id,
            Authentication authentication) {
        
        // Получаем ID пользователя из аутентификации
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Получаем существующий тест
        Optional<Test> existingTestOptional = testService.getTestById(id);
        if (existingTestOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Test existingTest = existingTestOptional.get();
        Long lessonId = existingTest.getLesson().getId();
        
        // Получаем ID курса для урока
        Optional<Lesson> lessonOptional = lessonService.getLessonById(lessonId);
        if (lessonOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Long courseId = lessonOptional.get().getCourse().getId();
        
        // Проверяем, принадлежит ли курс преподавателю
        if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Удаляем тест
        testService.deleteTest(id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Получить количество вопросов в тесте.
     *
     * @param id ID теста
     * @return количество вопросов
     */
    @GetMapping("/{id}/question-count")
    public ResponseEntity<Long> getQuestionCount(@PathVariable Long id) {
        // Проверяем существование теста
        if (testService.getTestById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        long count = testService.getQuestionCount(id);
        return ResponseEntity.ok(count);
    }

    /**
     * Получить максимальное количество баллов за тест.
     *
     * @param id ID теста
     * @return максимальное количество баллов
     */
    @GetMapping("/{id}/max-points")
    public ResponseEntity<Integer> getMaxPoints(@PathVariable Long id) {
        // Проверяем существование теста
        if (testService.getTestById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        int maxPoints = testService.getMaxPoints(id);
        return ResponseEntity.ok(maxPoints);
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
        
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            return Long.parseLong(((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername());
        } else if (authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }
        
        return null;
    }
}
