package com.example.course_app.entity.submissions;

import com.example.course_app.entity.tests.Test;
import com.example.course_app.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность, представляющая отправку студентом ответов на тест.
 */
@Entity
@Table(name = "student_submissions")
@Data
@NoArgsConstructor
public class StudentSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    /**
     * Время начала прохождения теста
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * Время завершения прохождения теста
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * Набранные баллы за тест
     */
    @Column
    private Integer score;

    /**
     * Флаг, указывающий, проверен ли тест преподавателем
     * (для тестов с ручной проверкой)
     */
    @Column
    private Boolean reviewed = false;

    /**
     * Список ответов студента на вопросы теста
     */
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentAnswer> answers = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Добавить ответ студента к отправке
     *
     * @param answer ответ студента для добавления
     * @return текущий экземпляр отправки для цепочки вызовов
     */
    public StudentSubmission addAnswer(StudentAnswer answer) {
        answers.add(answer);
        answer.setSubmission(this);
        return this;
    }

    /**
     * Удалить ответ студента из отправки
     *
     * @param answer ответ студента для удаления
     * @return текущий экземпляр отправки для цепочки вызовов
     */
    public StudentSubmission removeAnswer(StudentAnswer answer) {
        answers.remove(answer);
        answer.setSubmission(null);
        return this;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
