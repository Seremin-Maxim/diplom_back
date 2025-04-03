package com.example.course_app.dto;

import java.time.LocalDateTime;

/**
 * DTO для передачи данных о доступе к уроку.
 */
public class LessonAccessDTO {
    private Long lessonId;
    private String lessonTitle;
    private String accessToken;
    private LocalDateTime expiresAt;
    private String accessUrl;
    private boolean completed;

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
     * Получить токен доступа к уроку.
     *
     * @return токен доступа
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Установить токен доступа к уроку.
     *
     * @param accessToken токен доступа
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Получить время истечения токена доступа.
     *
     * @return время истечения токена
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Установить время истечения токена доступа.
     *
     * @param expiresAt время истечения токена
     */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Получить URL для доступа к уроку.
     *
     * @return URL доступа
     */
    public String getAccessUrl() {
        return accessUrl;
    }

    /**
     * Установить URL для доступа к уроку.
     *
     * @param accessUrl URL доступа
     */
    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    /**
     * Проверить, завершен ли урок.
     *
     * @return true, если урок завершен
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Установить статус завершения урока.
     *
     * @param completed статус завершения
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
