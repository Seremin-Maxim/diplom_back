package com.example.course_app.util;

import com.example.course_app.dto.CourseDTO;
import com.example.course_app.entity.courses.Course;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Утилитный класс для конвертации между сущностями Course и DTO CourseDTO.
 * Используется для избежания проблем с ленивой загрузкой связей в Hibernate.
 */
public class CourseMapper {

    /**
     * Конвертирует сущность Course в DTO CourseDTO.
     * 
     * @param course сущность курса
     * @return DTO курса
     */
    public static CourseDTO toDTO(Course course) {
        if (course == null) {
            return null;
        }
        
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setStatus(course.getStatus());
        dto.setPublic(course.isPublic());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        
        // Безопасно извлекаем данные преподавателя, если они доступны
        if (course.getTeacher() != null) {
            dto.setTeacherId(course.getTeacher().getId());
            dto.setTeacherFirstName(course.getTeacher().getFirstName());
            dto.setTeacherLastName(course.getTeacher().getLastName());
        }
        
        return dto;
    }
    
    /**
     * Конвертирует список сущностей Course в список DTO CourseDTO.
     * 
     * @param courses список сущностей курсов
     * @return список DTO курсов
     */
    public static List<CourseDTO> toDTOList(List<Course> courses) {
        if (courses == null) {
            return null;
        }
        
        return courses.stream()
                .map(CourseMapper::toDTO)
                .collect(Collectors.toList());
    }
}
