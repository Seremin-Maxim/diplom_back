package com.example.course_app.util;

import com.example.course_app.dto.LessonDTO;
import com.example.course_app.entity.lessons.Lesson;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Утилитный класс для конвертации между сущностями Lesson и DTO LessonDTO.
 * Используется для избежания проблем с ленивой загрузкой связей в Hibernate.
 */
public class LessonMapper {

    /**
     * Конвертирует сущность Lesson в DTO LessonDTO.
     * 
     * @param lesson сущность урока
     * @return DTO урока
     */
    public static LessonDTO toDTO(Lesson lesson) {
        if (lesson == null) {
            return null;
        }
        
        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setContent(lesson.getContent());
        
        dto.setIsContent(lesson.getIsContent());
        dto.setOrderNumber(lesson.getOrderNumber());
        dto.setTimeLimit(lesson.getTimeLimit());
        dto.setCreatedAt(lesson.getCreatedAt());
        dto.setUpdatedAt(lesson.getUpdatedAt());
        
        // Безопасно извлекаем данные курса, если они доступны
        if (lesson.getCourse() != null) {
            dto.setCourseId(lesson.getCourse().getId());
            dto.setCourseTitle(lesson.getCourse().getTitle());
            
            // Добавляем информацию об авторе (преподавателе курса) с проверкой на null
            try {
                if (lesson.getCourse().getTeacher() != null) {
                    dto.setAuthorId(lesson.getCourse().getTeacher().getId());
                    // Формируем полное имя из firstName и lastName с проверкой на null
                    String firstName = lesson.getCourse().getTeacher().getFirstName() != null ? 
                                      lesson.getCourse().getTeacher().getFirstName() : "";
                    String lastName = lesson.getCourse().getTeacher().getLastName() != null ? 
                                     lesson.getCourse().getTeacher().getLastName() : "";
                    String authorName = firstName + " " + lastName;
                    dto.setAuthorName(authorName.trim());
                } else {
                    dto.setAuthorId(null);
                    dto.setAuthorName("Не указан");
                }
            } catch (Exception e) {
                // В случае LazyInitializationException или другой ошибки
                dto.setAuthorId(null);
                dto.setAuthorName("Не указан");
            }
        }
        
        // В текущей реализации у Lesson нет поля test, поэтому не устанавливаем testId
        // Если в будущем будет добавлено поле test, раскомментируйте код ниже
        // if (lesson.getTest() != null) {
        //     dto.setTestId(lesson.getTest().getId());
        // }
        
        return dto;
    }
    
    /**
     * Конвертирует список сущностей Lesson в список DTO LessonDTO.
     * 
     * @param lessons список сущностей уроков
     * @return список DTO уроков
     */
    public static List<LessonDTO> toDTOList(List<Lesson> lessons) {
        if (lessons == null) {
            return null;
        }
        
        return lessons.stream()
                .map(LessonMapper::toDTO)
                .collect(Collectors.toList());
    }
}
