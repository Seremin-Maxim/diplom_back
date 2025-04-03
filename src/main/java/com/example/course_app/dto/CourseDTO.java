package com.example.course_app.dto;

import com.example.course_app.entity.courses.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для передачи данных о курсе между слоями приложения.
 * Используется для избежания проблем с ленивой загрузкой связей в Hibernate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private Long teacherId;
    private String teacherFirstName;
    private String teacherLastName;
    private String title;
    private String description;
    private CourseStatus status;
    private boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
