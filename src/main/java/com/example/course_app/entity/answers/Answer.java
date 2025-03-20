package com.example.course_app.entity.answers;

import com.example.course_app.entity.questions.Question;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность ответа, представляющая вариант ответа на вопрос.
 */
@Entity
@Table(name = "answers")
@Data
@NoArgsConstructor
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * Текст ответа
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    /**
     * Флаг, указывающий, является ли ответ правильным.
     * Для вопросов с одним правильным ответом (SINGLE_CHOICE) только один ответ должен иметь isCorrect = true.
     * Для вопросов с несколькими правильными ответами (MULTIPLE_CHOICE) несколько ответов могут иметь isCorrect = true.
     */
    @Column(name = "is_correct")
    private boolean isCorrect = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
