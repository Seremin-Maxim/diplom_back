package com.example.course_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных о вариантах ответов.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDTO {
    private Long id;
    private Long questionId;
    private String text;
    private boolean isCorrect;
    private String createdAt;
    private String updatedAt;
}
