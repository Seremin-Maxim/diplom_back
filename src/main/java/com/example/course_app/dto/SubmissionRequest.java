package com.example.course_app.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для запроса создания отправки ответов на тест.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRequest {

    /**
     * Идентификатор теста
     */
    @NotNull(message = "Идентификатор теста не может быть пустым")
    private Long testId;

    /**
     * Список ответов на вопросы
     */
    @NotEmpty(message = "Список ответов не может быть пустым")
    private List<AnswerRequest> answers;

    /**
     * DTO для запроса создания ответа на вопрос
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerRequest {
        
        /**
         * Идентификатор вопроса
         */
        @NotNull(message = "Идентификатор вопроса не может быть пустым")
        private Long questionId;
        
        /**
         * Список идентификаторов выбранных вариантов ответа (для вопросов с выбором)
         */
        private List<Long> selectedAnswerIds;
        
        /**
         * Текстовый ответ (для текстовых вопросов)
         */
        private String textAnswer;
    }
}
