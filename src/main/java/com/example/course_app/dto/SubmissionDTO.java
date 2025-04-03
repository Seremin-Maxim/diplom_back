package com.example.course_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для передачи информации об отправке ответов на тест.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDTO {

    /**
     * Идентификатор отправки
     */
    private Long id;
    
    /**
     * Идентификатор студента
     */
    private Long studentId;
    
    /**
     * Имя студента
     */
    private String studentName;
    
    /**
     * Идентификатор теста
     */
    private Long testId;
    
    /**
     * Название теста
     */
    private String testTitle;
    
    /**
     * Время начала выполнения теста
     */
    private LocalDateTime startTime;
    
    /**
     * Время завершения выполнения теста
     */
    private LocalDateTime endTime;
    
    /**
     * Полученная оценка
     */
    private Integer score;
    
    /**
     * Максимально возможная оценка
     */
    private Integer maxScore;
    
    /**
     * Флаг проверки отправки
     */
    private boolean reviewed;
}
