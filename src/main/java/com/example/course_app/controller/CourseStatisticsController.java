package com.example.course_app.controller;

import com.example.course_app.entity.User;
import com.example.course_app.service.UserService;
import com.example.course_app.service.courses.CourseStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Контроллер для получения статистики по курсам.
 * Предоставляет API для получения статистики по курсам и преподавателям.
 */
@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class CourseStatisticsController {

    private final CourseStatisticsService statisticsService;
    private final UserService userService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param statisticsService сервис для получения статистики по курсам
     * @param userService сервис для работы с пользователями
     */
    @Autowired
    public CourseStatisticsController(
            CourseStatisticsService statisticsService,
            UserService userService) {
        this.statisticsService = statisticsService;
        this.userService = userService;
    }

    /**
     * Получить статистику по курсу.
     *
     * @param courseId идентификатор курса
     * @return статистика по курсу
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Long>> getCourseStatistics(@PathVariable Long courseId) {
        Map<String, Long> statistics = statisticsService.getCourseStatistics(courseId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Получить статистику по преподавателю.
     *
     * @param teacherId идентификатор преподавателя
     * @return статистика по преподавателю
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<Map<String, Long>> getTeacherStatistics(@PathVariable Long teacherId) {
        Map<String, Long> statistics = statisticsService.getTeacherStatistics(teacherId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Получить статистику по текущему преподавателю.
     *
     * @return статистика по текущему преподавателю
     */
    @GetMapping("/teacher/me")
    public ResponseEntity<Map<String, Long>> getCurrentTeacherStatistics() {
        // Получаем текущего пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        // Проверяем, что пользователь является преподавателем
        if (!user.getRole().toString().equals("TEACHER") && !user.getRole().toString().equals("ROLE_TEACHER")) {
            return ResponseEntity.badRequest().build();
        }
        
        Map<String, Long> statistics = statisticsService.getTeacherStatistics(user.getId());
        return ResponseEntity.ok(statistics);
    }
}
