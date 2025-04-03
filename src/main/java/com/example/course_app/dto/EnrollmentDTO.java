package com.example.course_app.dto;

import java.time.LocalDateTime;

/**
 * DTO для передачи данных о записи на курс или урок.
 */
public class EnrollmentDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private Long lessonId;
    private String lessonTitle;
    private LocalDateTime joinedAt;
    private Boolean completed;

    /**
     * Получить идентификатор записи.
     *
     * @return идентификатор записи
     */
    public Long getId() {
        return id;
    }

    /**
     * Установить идентификатор записи.
     *
     * @param id идентификатор записи
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Получить идентификатор студента.
     *
     * @return идентификатор студента
     */
    public Long getStudentId() {
        return studentId;
    }

    /**
     * Установить идентификатор студента.
     *
     * @param studentId идентификатор студента
     */
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    /**
     * Получить имя студента.
     *
     * @return имя студента
     */
    public String getStudentName() {
        return studentName;
    }

    /**
     * Установить имя студента.
     *
     * @param studentName имя студента
     */
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    /**
     * Получить идентификатор курса.
     *
     * @return идентификатор курса
     */
    public Long getCourseId() {
        return courseId;
    }

    /**
     * Установить идентификатор курса.
     *
     * @param courseId идентификатор курса
     */
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    /**
     * Получить название курса.
     *
     * @return название курса
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * Установить название курса.
     *
     * @param courseName название курса
     */
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    /**
     * Получить идентификатор урока.
     *
     * @return идентификатор урока
     */
    public Long getLessonId() {
        return lessonId;
    }

    /**
     * Установить идентификатор урока.
     *
     * @param lessonId идентификатор урока
     */
    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    /**
     * Получить название урока.
     *
     * @return название урока
     */
    public String getLessonTitle() {
        return lessonTitle;
    }

    /**
     * Установить название урока.
     *
     * @param lessonTitle название урока
     */
    public void setLessonTitle(String lessonTitle) {
        this.lessonTitle = lessonTitle;
    }

    /**
     * Получить время записи.
     *
     * @return время записи
     */
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    /**
     * Установить время записи.
     *
     * @param joinedAt время записи
     */
    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    /**
     * Проверить, завершен ли курс или урок.
     *
     * @return статус завершения
     */
    public Boolean getCompleted() {
        return completed;
    }

    /**
     * Установить статус завершения курса или урока.
     *
     * @param completed статус завершения
     */
    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
