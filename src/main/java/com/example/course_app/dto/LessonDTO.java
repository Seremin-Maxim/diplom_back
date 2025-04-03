package com.example.course_app.dto;

import java.time.LocalDateTime;

/**
 * DTO для сущности Lesson.
 * Используется для передачи данных между клиентом и сервером без проблем с ленивой загрузкой Hibernate.
 */
public class LessonDTO {
    private Long id;
    private String title;
    private String content;
    private Boolean isContent;
    private Integer orderNumber;
    private Integer timeLimit;
    private Long courseId;
    private String courseTitle;
    private Long testId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Конструктор по умолчанию
     */
    public LessonDTO() {
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    

    
    public Boolean getIsContent() {
        return isContent;
    }
    
    public void setIsContent(Boolean isContent) {
        this.isContent = isContent;
    }
    
    public Integer getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public Integer getTimeLimit() {
        return timeLimit;
    }
    
    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    
    public String getCourseTitle() {
        return courseTitle;
    }
    
    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
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
