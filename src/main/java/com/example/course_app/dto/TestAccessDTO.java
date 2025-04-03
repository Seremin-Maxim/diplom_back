package com.example.course_app.dto;

import java.time.LocalDateTime;

/**
 * DTO для передачи данных о доступе к тесту.
 */
public class TestAccessDTO {
    private Long testId;
    private String testTitle;
    private String accessToken;
    private LocalDateTime expiresAt;
    private String accessUrl;
    private boolean completed;
    private Double score;

    /**
     * Получить идентификатор теста.
     *
     * @return идентификатор теста
     */
    public Long getTestId() {
        return testId;
    }

    /**
     * Установить идентификатор теста.
     *
     * @param testId идентификатор теста
     */
    public void setTestId(Long testId) {
        this.testId = testId;
    }

    /**
     * Получить название теста.
     *
     * @return название теста
     */
    public String getTestTitle() {
        return testTitle;
    }

    /**
     * Установить название теста.
     *
     * @param testTitle название теста
     */
    public void setTestTitle(String testTitle) {
        this.testTitle = testTitle;
    }

    /**
     * Получить токен доступа к тесту.
     *
     * @return токен доступа
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Установить токен доступа к тесту.
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
     * Получить URL для доступа к тесту.
     *
     * @return URL доступа
     */
    public String getAccessUrl() {
        return accessUrl;
    }

    /**
     * Установить URL для доступа к тесту.
     *
     * @param accessUrl URL доступа
     */
    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    /**
     * Проверить, завершен ли тест.
     *
     * @return true, если тест завершен
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Установить статус завершения теста.
     *
     * @param completed статус завершения
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Получить оценку за тест.
     *
     * @return оценка за тест
     */
    public Double getScore() {
        return score;
    }

    /**
     * Установить оценку за тест.
     *
     * @param score оценка за тест
     */
    public void setScore(Double score) {
        this.score = score;
    }
}
