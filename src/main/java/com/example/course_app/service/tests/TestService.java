package com.example.course_app.service.tests;

import com.example.course_app.entity.tests.Test;
import com.example.course_app.entity.tests.TestType;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с тестами.
 */
public interface TestService {
    
    /**
     * Создать новый тест для урока.
     * 
     * @param lessonId идентификатор урока
     * @param title название теста
     * @param type тип теста
     * @param requiresManualCheck требуется ли ручная проверка
     * @return созданный тест
     */
    Test createTest(Long lessonId, String title, TestType type, boolean requiresManualCheck);
    
    /**
     * Получить тест по идентификатору.
     * 
     * @param id идентификатор теста
     * @return тест или пустой Optional, если тест не найден
     */
    Optional<Test> getTestById(Long id);
    
    /**
     * Получить все тесты для урока.
     * 
     * @param lessonId идентификатор урока
     * @return список тестов урока
     */
    List<Test> getTestsByLessonId(Long lessonId);
    
    /**
     * Получить все тесты определенного типа.
     * 
     * @param type тип теста
     * @return список тестов указанного типа
     */
    List<Test> getTestsByType(TestType type);
    
    /**
     * Получить все тесты для урока определенного типа.
     * 
     * @param lessonId идентификатор урока
     * @param type тип теста
     * @return список тестов урока указанного типа
     */
    List<Test> getTestsByLessonIdAndType(Long lessonId, TestType type);
    
    /**
     * Получить все тесты, требующие ручной проверки.
     * 
     * @return список тестов с ручной проверкой
     */
    List<Test> getTestsRequiringManualCheck();
    
    /**
     * Обновить информацию о тесте.
     * 
     * @param id идентификатор теста
     * @param title новое название теста
     * @param type новый тип теста
     * @param requiresManualCheck требуется ли ручная проверка
     * @return обновленный тест
     */
    Test updateTest(Long id, String title, TestType type, boolean requiresManualCheck);
    
    /**
     * Удалить тест.
     * 
     * @param id идентификатор теста
     */
    void deleteTest(Long id);
    
    /**
     * Проверить, принадлежит ли тест уроку.
     * 
     * @param testId идентификатор теста
     * @param lessonId идентификатор урока
     * @return true, если тест принадлежит уроку
     */
    boolean isTestBelongsToLesson(Long testId, Long lessonId);
    
    /**
     * Проверить, требует ли тест ручной проверки.
     * 
     * @param testId идентификатор теста
     * @return true, если тест требует ручной проверки
     */
    boolean isTestRequiresManualCheck(Long testId);
    
    /**
     * Получить количество вопросов в тесте.
     * 
     * @param testId идентификатор теста
     * @return количество вопросов
     */
    long getQuestionCount(Long testId);
    
    /**
     * Получить максимальное количество баллов за тест.
     * 
     * @param testId идентификатор теста
     * @return максимальное количество баллов
     */
    int getMaxPoints(Long testId);
    
    /**
     * Проверить, создан ли тест указанным пользователем.
     * 
     * @param testId идентификатор теста
     * @param userId идентификатор пользователя
     * @return true, если тест создан указанным пользователем
     */
    boolean isTestCreatedByUser(Long testId, Long userId);
}
