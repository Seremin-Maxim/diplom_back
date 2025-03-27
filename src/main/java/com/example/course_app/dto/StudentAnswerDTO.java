package com.example.course_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для передачи данных об ответе студента на вопрос.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswerDTO {
    private Long id;
    private Long submissionId;
    private Long questionId;
    private String questionText;
    private String answerText;
    private Boolean isCorrect;
    private Integer score;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
