package com.example.course_app.entity.submissions;

import com.example.course_app.entity.questions.Question;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность, представляющая ответ студента на конкретный вопрос теста.
 */
@Entity
@Table(name = "student_answers")
@Data
@NoArgsConstructor
public class StudentAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private StudentSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * Текст ответа студента.
     * Для вопросов с выбором может содержать ID выбранных вариантов,
     * для текстовых вопросов - введенный текст,
     * для эссе - полный текст ответа.
     */
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    /**
     * Флаг, указывающий, является ли ответ правильным.
     * Для вопросов с автоматической проверкой устанавливается системой,
     * для вопросов с ручной проверкой - преподавателем.
     */
    @Column(name = "is_correct")
    private Boolean isCorrect = false;

    /**
     * Оценка за ответ.
     * Для автоматически проверяемых вопросов устанавливается системой,
     * для вопросов с ручной проверкой - преподавателем.
     * Может быть null до проверки ответа.
     */
    @Column(name = "score")
    private Integer score;

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
