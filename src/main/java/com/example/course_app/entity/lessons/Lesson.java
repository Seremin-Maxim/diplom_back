package com.example.course_app.entity.lessons;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.course_app.entity.courses.Course;

/**
 * Сущность урока, представляющая отдельный урок в рамках курса.
 * Урок содержит учебный материал и может иметь тесты для проверки знаний.
 */
@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * Флаг, указывающий, является ли урок только контентом (без тестов).
     * True - урок содержит только информационный материал.
     * False - урок содержит тесты или другие интерактивные элементы.
     */
    @Column(name = "is_content")
    private boolean isContent = false;

    /**
     * Ограничение времени на прохождение урока в минутах.
     * Null означает отсутствие ограничения.
     */
    @Column(name = "time_limit")
    private Integer timeLimit;

    /**
     * Порядковый номер урока в курсе.
     */
    @Column(name = "order_number")
    private Integer orderNumber = 0;

    /**
     * Уникальный код для доступа к уроку.
     * Может использоваться для генерации ссылок.
     */
    @Column(name = "unique_code", unique = true, length = 50)
    private String uniqueCode;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void setIsContent(boolean isContent) {
        this.isContent = isContent;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Генерация уникального кода, если он не был установлен
        if (uniqueCode == null || uniqueCode.isEmpty()) {
            uniqueCode = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean getIsContent(){
        return isContent;
    }
}
