package com.example.course_app.entity.questions;

import com.example.course_app.entity.answers.Answer;
import com.example.course_app.entity.tests.Test;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность вопроса, представляющая вопрос в тесте.
 */
@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    /**
     * Текст вопроса
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    /**
     * Тип вопроса (например, SINGLE_CHOICE, MULTIPLE_CHOICE, TEXT_INPUT и т.д.)
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionType type;

    /**
     * Количество баллов за правильный ответ на вопрос
     */
    @Column
    private Integer points = 1;
    
    /**
     * Список вариантов ответов на вопрос
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Добавить вариант ответа к вопросу
     * 
     * @param answer вариант ответа для добавления
     * @return текущий экземпляр вопроса для цепочки вызовов
     */
    public Question addAnswer(Answer answer) {
        answers.add(answer);
        answer.setQuestion(this);
        return this;
    }
    
    /**
     * Удалить вариант ответа из вопроса
     * 
     * @param answer вариант ответа для удаления
     * @return текущий экземпляр вопроса для цепочки вызовов
     */
    public Question removeAnswer(Answer answer) {
        answers.remove(answer);
        answer.setQuestion(null);
        return this;
    }

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
