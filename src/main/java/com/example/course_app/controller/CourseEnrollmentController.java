package com.example.course_app.controller;

import com.example.course_app.dto.EnrollmentDTO;
import com.example.course_app.entity.User;
import com.example.course_app.entity.enrollments.CourseEnrollment;
import com.example.course_app.service.courses.CourseEnrollmentService;
import com.example.course_app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для управления записями студентов на курсы.
 */
@RestController
@RequestMapping("/api/enrollments/courses")
public class CourseEnrollmentController {

    private final CourseEnrollmentService enrollmentService;
    private final UserService userService;

    /**
     * Конструктор контроллера.
     *
     * @param enrollmentService сервис для управления записями на курсы
     * @param userService сервис для работы с пользователями
     */
    @Autowired
    public CourseEnrollmentController(CourseEnrollmentService enrollmentService, UserService userService) {
        this.enrollmentService = enrollmentService;
        this.userService = userService;
    }

    /**
     * Записать текущего пользователя на курс.
     *
     * @param courseId идентификатор курса
     * @return информация о записи
     */
    @PostMapping("/{courseId}")
    public ResponseEntity<?> enrollCurrentUserToCourse(@PathVariable Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        try {
            CourseEnrollment enrollment = enrollmentService.enrollStudentToCourse(user.getId(), courseId);
            return ResponseEntity.ok(convertToDTO(enrollment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Отписать текущего пользователя от курса.
     *
     * @param courseId идентификатор курса
     * @return статус операции
     */
    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> unenrollCurrentUserFromCourse(@PathVariable Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        try {
            enrollmentService.unenrollStudentFromCourse(user.getId(), courseId);
            return ResponseEntity.ok("Вы успешно отписались от курса");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Получить все записи текущего пользователя на курсы.
     *
     * @return список записей
     */
    @GetMapping("/my")
    public ResponseEntity<List<EnrollmentDTO>> getCurrentUserEnrollments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        List<CourseEnrollment> enrollments = enrollmentService.getEnrollmentsByStudentId(user.getId());
        List<EnrollmentDTO> enrollmentDTOs = enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(enrollmentDTOs);
    }

    /**
     * Получить все записи на курс (только для преподавателей и администраторов).
     *
     * @param courseId идентификатор курса
     * @return список записей
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByCourseId(@PathVariable Long courseId) {
        List<CourseEnrollment> enrollments = enrollmentService.getEnrollmentsByCourseId(courseId);
        List<EnrollmentDTO> enrollmentDTOs = enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(enrollmentDTOs);
    }

    /**
     * Проверить, записан ли текущий пользователь на курс.
     *
     * @param courseId идентификатор курса
     * @return статус записи
     */
    @GetMapping("/{courseId}/status")
    public ResponseEntity<Boolean> isCurrentUserEnrolled(@PathVariable Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(user.getId(), courseId);
        return ResponseEntity.ok(isEnrolled);
    }

    /**
     * Получить количество студентов, записанных на курс.
     *
     * @param courseId идентификатор курса
     * @return количество студентов
     */
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Long> getEnrollmentCount(@PathVariable Long courseId) {
        long count = enrollmentService.getEnrollmentCountForCourse(courseId);
        return ResponseEntity.ok(count);
    }

    /**
     * Преобразовать сущность записи в DTO.
     *
     * @param enrollment сущность записи
     * @return DTO записи
     */
    private EnrollmentDTO convertToDTO(CourseEnrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setStudentId(enrollment.getStudent().getId());
        dto.setStudentName(enrollment.getStudent().getUsername());
        dto.setCourseId(enrollment.getCourse().getId());
        dto.setCourseName(enrollment.getCourse().getTitle());
        dto.setJoinedAt(enrollment.getJoinedAt());
        dto.setCompleted(enrollment.getCompleted());
        return dto;
    }
}
