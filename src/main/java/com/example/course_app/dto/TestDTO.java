package com.example.course_app.dto;

import com.example.course_app.entity.tests.TestType;
import java.time.LocalDateTime;

/**
 * DTO для сущности Test
 */
public class TestDTO {
    private Long id;
    private String title;
    private TestType type;
    private boolean requiresManualCheck;
    private Long lessonId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TestDTO() {
    }

    public TestDTO(Long id, String title, TestType type, boolean requiresManualCheck, 
                  Long lessonId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.requiresManualCheck = requiresManualCheck;
        this.lessonId = lessonId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TestType getType() {
        return type;
    }

    public void setType(TestType type) {
        this.type = type;
    }

    public boolean isRequiresManualCheck() {
        return requiresManualCheck;
    }

    public void setRequiresManualCheck(boolean requiresManualCheck) {
        this.requiresManualCheck = requiresManualCheck;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
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
