package com.example.course_app.entity.tests;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.entity.questions.Question;

/**
 * Сущность теста, представляющая тест для проверки знаний в рамках урока.
 */
@Entity
@Table(name = "tests")
@Data
@NoArgsConstructor
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private String title;

    /**
     * Тип теста (например, MULTIPLE_CHOICE, ESSAY, CODING и т.д.)
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TestType type;

    /**
     * Флаг, указывающий, требуется ли ручная проверка теста преподавателем.
     */
    @Column(name = "requires_manual_check")
    private boolean requiresManualCheck = false;
    
    /**
     * Список вопросов в тесте
     */
    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Добавить вопрос в тест
     * 
     * @param question вопрос для добавления
     * @return текущий экземпляр теста для цепочки вызовов
     */
    public Test addQuestion(Question question) {
        questions.add(question);
        question.setTest(this);
        return this;
    }
    
    /**
     * Удалить вопрос из теста
     * 
     * @param question вопрос для удаления
     * @return текущий экземпляр теста для цепочки вызовов
     */
    public Test removeQuestion(Question question) {
        questions.remove(question);
        question.setTest(null);
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
