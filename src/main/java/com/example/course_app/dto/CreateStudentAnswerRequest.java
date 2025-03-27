package com.example.course_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для создания нового ответа студента.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudentAnswerRequest {
    /**
     * Идентификатор отправки, к которой относится ответ
     */
    @NotNull(message = "ID отправки не может быть пустым")
    private Long submissionId;
    
    /**
     * Идентификатор вопроса, на который отвечает студент
     */
    @NotNull(message = "ID вопроса не может быть пустым")
    private Long questionId;
    
    /**
     * Текст ответа студента
     */
    @NotBlank(message = "Текст ответа не может быть пустым")
    private String answerText;
}
