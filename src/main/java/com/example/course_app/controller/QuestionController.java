package com.example.course_app.controller;

import com.example.course_app.entity.questions.Question;
import com.example.course_app.entity.questions.QuestionType;
import com.example.course_app.entity.User;
import com.example.course_app.entity.tests.Test;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.dto.QuestionDTO;
import com.example.course_app.dto.QuestionMapper;
import com.example.course_app.service.courses.CourseService;
import com.example.course_app.service.lessons.LessonService;
import com.example.course_app.service.tests.TestService;
import com.example.course_app.service.questions.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * Контроллер для работы с вопросами.
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final TestService testService;
    private final LessonService lessonService;
    private final CourseService courseService;

    @Autowired
    public QuestionController(
            QuestionService questionService,
            TestService testService,
            LessonService lessonService,
            CourseService courseService) {
        this.questionService = questionService;
        this.testService = testService;
        this.lessonService = lessonService;
        this.courseService = courseService;
    }

    /**
     * Получить вопрос по ID.
     *
     * @param id ID вопроса
     * @return вопрос или статус 404, если вопрос не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> getQuestionById(@PathVariable Long id) {
        Optional<Question> questionOptional = questionService.getQuestionById(id);
        return questionOptional.map(question -> ResponseEntity.ok(QuestionMapper.toDTO(question)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Получить все вопросы для теста.
     *
     * @param testId ID теста
     * @return список вопросов
     */
    @GetMapping("/test/{testId}")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByTestId(@PathVariable Long testId) {
        List<Question> questions = questionService.getQuestionsByTestId(testId);
        return ResponseEntity.ok(QuestionMapper.toDTOList(questions));
    }

    /**
     * Получить все вопросы определенного типа для теста.
     *
     * @param testId ID теста
     * @param type тип вопроса
     * @return список вопросов
     */
    @GetMapping("/test/{testId}/type/{type}")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByTestIdAndType(
            @PathVariable Long testId,
            @PathVariable QuestionType type) {
        List<Question> questions = questionService.getQuestionsByTestIdAndType(testId, type);
        return ResponseEntity.ok(QuestionMapper.toDTOList(questions));
    }

    /**
     * Создать новый вопрос для теста.
     *
     * @param testId ID теста
     * @param requestBody тело запроса с данными вопроса
     * @param authentication объект аутентификации
     * @return созданный вопрос и статус 201
     */
    @PostMapping("/test/{testId}")
    public ResponseEntity<QuestionDTO> createQuestion(
            @PathVariable Long testId,
            @RequestBody Map<String, Object> requestBody,
            Authentication authentication) {
        
        // Получаем ID пользователя из аутентификации
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Получаем тест
        Optional<Test> testOptional = testService.getTestById(testId);
        if (testOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Test test = testOptional.get();
        Long lessonId = test.getLesson().getId();
        
        // Получаем урок и курс
        Optional<Lesson> lessonOptional = lessonService.getLessonById(lessonId);
        if (lessonOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Long courseId = lessonOptional.get().getCourse().getId();
        
        // Проверяем, принадлежит ли курс преподавателю
        if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Извлекаем данные из запроса
        String text = (String) requestBody.get("text");
        QuestionType type = QuestionType.valueOf((String) requestBody.get("type"));
        Integer points = requestBody.get("points") != null ? 
                Integer.valueOf(requestBody.get("points").toString()) : null;
        
        // Создаем вопрос
        Question createdQuestion = questionService.createQuestion(testId, text, type, points);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(QuestionMapper.toDTO(createdQuestion));
    }

    /**
     * Обновить существующий вопрос.
     *
     * @param id ID вопроса
     * @param requestBody тело запроса с обновленными данными
     * @param authentication объект аутентификации
     * @return обновленный вопрос
     */
    @PutMapping("/{id}")
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody,
            Authentication authentication) {
        
        // Получаем ID пользователя из аутентификации
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Получаем существующий вопрос
        Optional<Question> existingQuestionOptional = questionService.getQuestionById(id);
        if (existingQuestionOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Question existingQuestion = existingQuestionOptional.get();
        Long testId = existingQuestion.getTest().getId();
        
        // Получаем тест
        Optional<Test> testOptional = testService.getTestById(testId);
        if (testOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Test test = testOptional.get();
        Long lessonId = test.getLesson().getId();
        
        // Получаем урок и курс
        Optional<Lesson> lessonOptional = lessonService.getLessonById(lessonId);
        if (lessonOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Long courseId = lessonOptional.get().getCourse().getId();
        
        // Проверяем, принадлежит ли курс преподавателю
        if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Извлекаем данные из запроса
        String text = (String) requestBody.get("text");
        QuestionType type = QuestionType.valueOf((String) requestBody.get("type"));
        Integer points = requestBody.get("points") != null ? 
                Integer.valueOf(requestBody.get("points").toString()) : null;
        
        // Обновляем вопрос
        Question updatedQuestion = questionService.updateQuestion(id, text, type, points);
        
        return ResponseEntity.ok(QuestionMapper.toDTO(updatedQuestion));
    }

    /**
     * Удалить вопрос.
     *
     * @param id ID вопроса
     * @param authentication объект аутентификации
     * @return статус 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            Authentication authentication) {
        
        // Получаем ID пользователя из аутентификации
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Получаем существующий вопрос
        Optional<Question> existingQuestionOptional = questionService.getQuestionById(id);
        if (existingQuestionOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Question existingQuestion = existingQuestionOptional.get();
        Long testId = existingQuestion.getTest().getId();
        
        // Получаем тест
        Optional<Test> testOptional = testService.getTestById(testId);
        if (testOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Test test = testOptional.get();
        Long lessonId = test.getLesson().getId();
        
        // Получаем урок и курс
        Optional<Lesson> lessonOptional = lessonService.getLessonById(lessonId);
        if (lessonOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Long courseId = lessonOptional.get().getCourse().getId();
        
        // Проверяем, принадлежит ли курс преподавателю
        if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Удаляем вопрос
        questionService.deleteQuestion(id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Получить количество правильных ответов для вопроса.
     *
     * @param id ID вопроса
     * @return количество правильных ответов
     */
    @GetMapping("/{id}/correct-answers-count")
    public ResponseEntity<Long> getCorrectAnswersCount(@PathVariable Long id) {
        // Проверяем существование вопроса
        if (questionService.getQuestionById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        long count = questionService.getCorrectAnswersCount(id);
        return ResponseEntity.ok(count);
    }

    /**
     * Проверить, принадлежит ли вопрос тесту.
     *
     * @param questionId ID вопроса
     * @param testId ID теста
     * @return true, если вопрос принадлежит тесту, иначе false
     */
    @GetMapping("/{questionId}/belongs-to-test/{testId}")
    public ResponseEntity<Boolean> isQuestionBelongsToTest(
            @PathVariable Long questionId,
            @PathVariable Long testId) {
        // Проверяем существование вопроса
        if (questionService.getQuestionById(questionId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Проверяем существование теста
        if (testService.getTestById(testId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        boolean belongs = questionService.isQuestionBelongsToTest(questionId, testId);
        return ResponseEntity.ok(belongs);
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
