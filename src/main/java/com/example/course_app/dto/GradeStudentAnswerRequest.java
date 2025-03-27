package com.example.course_app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для оценки ответа студента преподавателем.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeStudentAnswerRequest {
    /**
     * Флаг, указывающий, является ли ответ правильным
     */
    @NotNull(message = "Флаг правильности ответа не может быть пустым")
    private Boolean isCorrect;
    
    /**
     * Оценка за ответ
     */
    @NotNull(message = "Оценка не может быть пустой")
    @Min(value = 0, message = "Оценка не может быть меньше 0")
    @Max(value = 100, message = "Оценка не может быть больше 100")
    private Integer score;
}
