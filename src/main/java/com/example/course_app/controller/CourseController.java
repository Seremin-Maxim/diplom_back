package com.example.course_app.controller;

import com.example.course_app.dto.CourseDTO;
import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.courses.CourseStatus;
import com.example.course_app.service.courses.CourseService;
import com.example.course_app.util.CourseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Получение всех курсов
     * @return список всех курсов
     */
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseEntity.ok(CourseMapper.toDTOList(courses));
    }

    /**
     * Получение курса по ID
     * @param id ID курса
     * @return курс или 404, если не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        Optional<Course> course = courseService.getCourseById(id);
        return course.map(c -> ResponseEntity.ok(CourseMapper.toDTO(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создание нового курса
     * @param course данные курса
     * @param authentication данные аутентификации
     * @return созданный курс
     */
    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@RequestBody Course course, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course createdCourse = courseService.createCourse(course, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CourseMapper.toDTO(createdCourse));
    }

    /**
     * Обновление курса
     * @param id ID курса
     * @param course новые данные курса
     * @param authentication данные аутентификации
     * @return обновленный курс
     */
    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody Course course, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        if (!courseService.isCourseOwnedByTeacher(id, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Course updatedCourse = courseService.updateCourse(id, course);
        return ResponseEntity.ok(CourseMapper.toDTO(updatedCourse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        if (!courseService.isCourseOwnedByTeacher(id, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Изменение статуса курса
     * @param id ID курса
     * @param status новый статус курса
     * @param authentication данные аутентификации
     * @return обновленный курс
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<CourseDTO> changeCourseStatus(@PathVariable Long id, @RequestBody CourseStatus status, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        if (!courseService.isCourseOwnedByTeacher(id, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Course updatedCourse = courseService.changeCourseStatus(id, status);
        return ResponseEntity.ok(CourseMapper.toDTO(updatedCourse));
    }

    /**
     * Изменение публичности курса
     * @param id ID курса
     * @param isPublic флаг публичности
     * @param authentication данные аутентификации
     * @return обновленный курс
     */
    @PatchMapping("/{id}/publicity")
    public ResponseEntity<CourseDTO> changeCoursePublicity(@PathVariable Long id, @RequestBody Boolean isPublic, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        if (!courseService.isCourseOwnedByTeacher(id, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Course updatedCourse = courseService.changeCoursePublicity(id, isPublic);
        return ResponseEntity.ok(CourseMapper.toDTO(updatedCourse));
    }

    /**
     * Получение курсов преподавателя
     * @param teacherId ID преподавателя
     * @return список курсов преподавателя
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseDTO>> getTeacherCourses(@PathVariable Long teacherId) {
        List<Course> courses = courseService.getCoursesByTeacherId(teacherId);
        return ResponseEntity.ok(CourseMapper.toDTOList(courses));
    }

    /**
     * Получение публичных курсов
     * @return список публичных курсов
     */
    @GetMapping("/public")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CourseDTO>> getPublicCourses() {
        List<Course> courses = courseService.getPublicCourses();
        return ResponseEntity.ok(CourseMapper.toDTOList(courses));
    }
}
