package com.example.course_app.dto;

import com.example.course_app.entity.questions.QuestionType;
import java.time.LocalDateTime;

/**
 * DTO для сущности Question
 */
public class QuestionDTO {
    private Long id;
    private String text;
    private QuestionType type;
    private Integer points;
    private Long testId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public QuestionDTO() {
    }

    public QuestionDTO(Long id, String text, QuestionType type, Integer points, 
                      Long testId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.points = points;
        this.testId = testId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
