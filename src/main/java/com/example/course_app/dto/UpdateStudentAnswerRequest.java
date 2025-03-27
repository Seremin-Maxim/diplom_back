package com.example.course_app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для обновления ответа студента.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentAnswerRequest {
    /**
     * Новый текст ответа студента
     */
    @NotBlank(message = "Текст ответа не может быть пустым")
    private String answerText;
}
