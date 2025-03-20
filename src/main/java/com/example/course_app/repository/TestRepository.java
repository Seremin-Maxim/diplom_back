package com.example.course_app.repository;

import com.example.course_app.entity.tests.Test;
import com.example.course_app.entity.tests.TestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с тестами.
 */
@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    
    /**
     * Найти все тесты для конкретного урока.
     * 
     * @param lessonId идентификатор урока
     * @return список тестов урока
     */
    List<Test> findByLessonId(Long lessonId);
    
    /**
     * Найти все тесты определенного типа.
     * 
     * @param type тип теста
     * @return список тестов указанного типа
     */
    List<Test> findByType(TestType type);
    
    /**
     * Найти все тесты с ручной проверкой.
     * 
     * @return список тестов с ручной проверкой
     */
    List<Test> findByRequiresManualCheckTrue();
    
    /**
     * Найти все тесты для конкретного урока определенного типа.
     * 
     * @param lessonId идентификатор урока
     * @param type тип теста
     * @return список тестов урока указанного типа
     */
    List<Test> findByLessonIdAndType(Long lessonId, TestType type);
}
