package com.example.course_app.controller;

import com.example.course_app.dto.EnrollmentDTO;
import com.example.course_app.dto.LessonAccessDTO;
import com.example.course_app.dto.LessonDTO;
import com.example.course_app.entity.User;
import com.example.course_app.entity.enrollments.LessonEnrollment;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.service.lessons.LessonEnrollmentService;
import com.example.course_app.service.lessons.LessonService;
import com.example.course_app.service.UserService;
import com.example.course_app.util.LessonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для управления записями студентов на уроки.
 */
@RestController
@RequestMapping("/api/enrollments/lessons")
public class LessonEnrollmentController {

    private final LessonEnrollmentService enrollmentService;
    private final LessonService lessonService;
    private final UserService userService;
    
    // Хранилище токенов доступа к урокам (в реальном приложении лучше использовать Redis или другое хранилище)
    private final Map<String, LessonAccessDTO> accessTokensMap = new HashMap<>();

    /**
     * Конструктор контроллера.
     *
     * @param enrollmentService сервис для управления записями на уроки
     * @param lessonService сервис для работы с уроками
     * @param userService сервис для работы с пользователями
     */
    @Autowired
    public LessonEnrollmentController(
            LessonEnrollmentService enrollmentService,
            LessonService lessonService,
            UserService userService) {
        this.enrollmentService = enrollmentService;
        this.lessonService = lessonService;
        this.userService = userService;
    }

    /**
     * Записать текущего пользователя на урок.
     *
     * @param lessonId идентификатор урока
     * @return информация о записи
     */
    @PostMapping("/{lessonId}")
    public ResponseEntity<?> enrollCurrentUserToLesson(@PathVariable Long lessonId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        try {
            LessonEnrollment enrollment = enrollmentService.enrollStudentToLesson(user.getId(), lessonId);
            return ResponseEntity.ok(convertToDTO(enrollment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Отметить урок как завершенный для текущего пользователя.
     *
     * @param lessonId идентификатор урока
     * @return обновленная информация о записи
     */
    @PutMapping("/{lessonId}/complete")
    public ResponseEntity<?> completeLessonForCurrentUser(@PathVariable Long lessonId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        try {
            LessonEnrollment enrollment = enrollmentService.completeLessonForStudent(user.getId(), lessonId);
            return ResponseEntity.ok(convertToDTO(enrollment));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Получить все записи текущего пользователя на уроки.
     *
     * @return список записей
     */
    @GetMapping("/my")
    public ResponseEntity<List<EnrollmentDTO>> getCurrentUserEnrollments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        List<LessonEnrollment> enrollments = enrollmentService.getEnrollmentsByStudentId(user.getId());
        List<EnrollmentDTO> enrollmentDTOs = enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(enrollmentDTOs);
    }

    /**
     * Получить все завершенные уроки текущего пользователя.
     *
     * @return список записей
     */
    @GetMapping("/my/completed")
    public ResponseEntity<List<EnrollmentDTO>> getCurrentUserCompletedLessons() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        List<LessonEnrollment> enrollments = enrollmentService.getCompletedLessonsByStudentId(user.getId());
        List<EnrollmentDTO> enrollmentDTOs = enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(enrollmentDTOs);
    }

    /**
     * Получить все записи на урок (только для преподавателей и администраторов).
     *
     * @param lessonId идентификатор урока
     * @return список записей
     */
    @GetMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByLessonId(@PathVariable Long lessonId) {
        List<LessonEnrollment> enrollments = enrollmentService.getEnrollmentsByLessonId(lessonId);
        List<EnrollmentDTO> enrollmentDTOs = enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(enrollmentDTOs);
    }

    /**
     * Проверить, записан ли текущий пользователь на урок.
     *
     * @param lessonId идентификатор урока
     * @return статус записи
     */
    @GetMapping("/{lessonId}/status")
    public ResponseEntity<Boolean> isCurrentUserEnrolled(@PathVariable Long lessonId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        boolean isEnrolled = enrollmentService.isStudentEnrolledInLesson(user.getId(), lessonId);
        return ResponseEntity.ok(isEnrolled);
    }

    /**
     * Проверить, завершил ли текущий пользователь урок.
     *
     * @param lessonId идентификатор урока
     * @return статус завершения
     */
    @GetMapping("/{lessonId}/completed")
    public ResponseEntity<Boolean> hasCurrentUserCompletedLesson(@PathVariable Long lessonId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        boolean hasCompleted = enrollmentService.hasStudentCompletedLesson(user.getId(), lessonId);
        return ResponseEntity.ok(hasCompleted);
    }

    /**
     * Получить прогресс текущего пользователя по курсу.
     *
     * @param courseId идентификатор курса
     * @return процент завершенных уроков
     */
    @GetMapping("/course/{courseId}/progress")
    public ResponseEntity<Double> getCurrentUserProgressInCourse(@PathVariable Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        double progress = enrollmentService.getStudentProgressInCourse(user.getId(), courseId);
        return ResponseEntity.ok(progress);
    }
    
    /**
     * Получить список доступных уроков для текущего пользователя в курсе.
     *
     * @param courseId идентификатор курса
     * @return список доступных уроков
     */
    @GetMapping("/course/{courseId}/available-lessons")
    public ResponseEntity<List<LessonAccessDTO>> getAvailableLessonsForCurrentUser(@PathVariable Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        // Проверяем, записан ли студент на курс
        if (!enrollmentService.isStudentEnrolledInCourse(user.getId(), courseId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Получаем список уроков курса
        List<Lesson> lessons = lessonService.getLessonsByCourseId(courseId);
        
        // Формируем список доступных уроков с токенами доступа
        List<LessonAccessDTO> accessDTOs = lessons.stream()
                .map(lesson -> createOrGetLessonAccessDTO(user.getId(), lesson))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(accessDTOs);
    }
    
    /**
     * Получить доступ к уроку по токену.
     *
     * @param lessonId идентификатор урока
     * @param token токен доступа
     * @return данные урока
     */
    @GetMapping("/access/{lessonId}")
    public ResponseEntity<?> accessLessonByToken(
            @PathVariable Long lessonId,
            @RequestParam("token") String token) {
        
        // Проверяем токен доступа
        LessonAccessDTO accessDTO = accessTokensMap.get(token);
        if (accessDTO == null || !accessDTO.getLessonId().equals(lessonId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недействительный токен доступа");
        }
        
        // Проверяем срок действия токена
        if (accessDTO.getExpiresAt().isBefore(LocalDateTime.now())) {
            accessTokensMap.remove(token);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Срок действия токена истек");
        }
        
        // Получаем данные урока
        Optional<Lesson> lessonOpt = lessonService.getLessonById(lessonId);
        if (lessonOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Возвращаем данные урока
        Lesson lesson = lessonOpt.get();
        LessonDTO lessonDTO = LessonMapper.toDTO(lesson);
        return ResponseEntity.ok(lessonDTO);
    }
    
    /**
     * Создать или получить существующий токен доступа к уроку.
     *
     * @param studentId идентификатор студента
     * @param lesson урок
     * @return DTO доступа к уроку
     */
    private LessonAccessDTO createOrGetLessonAccessDTO(Long studentId, Lesson lesson) {
        // Проверяем, завершил ли студент урок
        boolean completed = enrollmentService.hasStudentCompletedLesson(studentId, lesson.getId());
        
        // Генерируем уникальный токен доступа
        String accessToken = UUID.randomUUID().toString();
        
        // Создаем DTO доступа к уроку
        LessonAccessDTO accessDTO = new LessonAccessDTO();
        accessDTO.setLessonId(lesson.getId());
        accessDTO.setLessonTitle(lesson.getTitle());
        accessDTO.setAccessToken(accessToken);
        accessDTO.setExpiresAt(LocalDateTime.now().plusDays(1)); // Токен действует 1 день
        accessDTO.setAccessUrl("/api/enrollments/lessons/access/" + lesson.getId() + "?token=" + accessToken);
        accessDTO.setCompleted(completed);
        
        // Сохраняем токен в хранилище
        accessTokensMap.put(accessToken, accessDTO);
        
        return accessDTO;
    }

    /**
     * Преобразовать сущность записи в DTO.
     *
     * @param enrollment сущность записи
     * @return DTO записи
     */
    private EnrollmentDTO convertToDTO(LessonEnrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setStudentId(enrollment.getStudent().getId());
        dto.setStudentName(enrollment.getStudent().getUsername());
        dto.setLessonId(enrollment.getLesson().getId());
        dto.setLessonTitle(enrollment.getLesson().getTitle());
        dto.setJoinedAt(enrollment.getJoinedAt());
        dto.setCompleted(enrollment.getCompleted());
        return dto;
    }
}
