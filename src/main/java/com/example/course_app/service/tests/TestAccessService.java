package com.example.course_app.service.tests;

import com.example.course_app.dto.TestAccessDTO;
import com.example.course_app.entity.tests.Test;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления доступом к тестам.
 */
public interface TestAccessService {
    
    /**
     * Получить список доступных тестов для студента в уроке.
     *
     * @param studentId идентификатор студента
     * @param lessonId идентификатор урока
     * @return список доступных тестов
     */
    List<Test> getAvailableTestsForStudent(Long studentId, Long lessonId);
    
    /**
     * Проверить, имеет ли студент доступ к тесту.
     *
     * @param studentId идентификатор студента
     * @param testId идентификатор теста
     * @return true, если студент имеет доступ к тесту
     */
    boolean hasStudentAccessToTest(Long studentId, Long testId);
    
    /**
     * Проверить, отправил ли студент ответы на тест.
     *
     * @param studentId идентификатор студента
     * @param testId идентификатор теста
     * @return true, если студент отправил ответы на тест
     */
    boolean hasStudentSubmittedTest(Long studentId, Long testId);
    
    /**
     * Получить оценку студента за тест.
     *
     * @param studentId идентификатор студента
     * @param testId идентификатор теста
     * @return оценка студента за тест или null, если тест не пройден
     */
    Double getStudentTestScore(Long studentId, Long testId);
    
    /**
     * Создать токен доступа к тесту для студента.
     *
     * @param studentId идентификатор студента
     * @param test тест
     * @return DTO с информацией о доступе к тесту
     */
    TestAccessDTO createTestAccessToken(Long studentId, Test test);
    
    /**
     * Проверить валидность токена доступа к тесту.
     *
     * @param token токен доступа
     * @param testId идентификатор теста
     * @return DTO с информацией о доступе к тесту или пустой Optional, если токен недействителен
     */
    Optional<TestAccessDTO> validateTestAccessToken(String token, Long testId);
}
