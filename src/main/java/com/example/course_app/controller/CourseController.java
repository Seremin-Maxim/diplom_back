package com.example.course_app.controller;

import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.courses.CourseStatus;
import com.example.course_app.service.courses.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        Optional<Course> course = courseService.getCourseById(id);
        return course.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course createdCourse = courseService.createCourse(course, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course course, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        if (!courseService.isCourseOwnedByTeacher(id, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Course updatedCourse = courseService.updateCourse(id, course);
        return ResponseEntity.ok(updatedCourse);
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

    @PatchMapping("/{id}/status")
    public ResponseEntity<Course> changeCourseStatus(@PathVariable Long id, @RequestBody CourseStatus status, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        if (!courseService.isCourseOwnedByTeacher(id, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Course updatedCourse = courseService.changeCourseStatus(id, status);
        return ResponseEntity.ok(updatedCourse);
    }

    @PatchMapping("/{id}/publicity")
    public ResponseEntity<Course> changeCoursePublicity(@PathVariable Long id, @RequestBody Boolean isPublic, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        if (!courseService.isCourseOwnedByTeacher(id, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Course updatedCourse = courseService.changeCoursePublicity(id, isPublic);
        return ResponseEntity.ok(updatedCourse);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Course>> getTeacherCourses(@PathVariable Long teacherId) {
        List<Course> courses = courseService.getCoursesByTeacherId(teacherId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Course>> getPublicCourses() {
        List<Course> courses = courseService.getPublicCourses();
        return ResponseEntity.ok(courses);
    }
}
