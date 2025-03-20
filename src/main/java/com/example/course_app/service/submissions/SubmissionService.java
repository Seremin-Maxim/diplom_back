package com.example.course_app.service.submissions;

import com.example.course_app.entity.submissions.StudentSubmission;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с отправками ответов студентов на тесты.
 */
public interface SubmissionService {
    
    /**
     * Создать новую отправку ответов студента на тест.
     * 
     * @param studentId идентификатор студента
     * @param testId идентификатор теста
     * @return созданная отправка
     */
    StudentSubmission createSubmission(Long studentId, Long testId);
    
    /**
     * Получить отправку по идентификатору.
     * 
     * @param id идентификатор отправки
     * @return отправка или пустой Optional, если отправка не найдена
     */
    Optional<StudentSubmission> getSubmissionById(Long id);
    
    /**
     * Получить все отправки конкретного студента.
     * 
     * @param studentId идентификатор студента
     * @return список отправок студента
     */
    List<StudentSubmission> getSubmissionsByStudentId(Long studentId);
    
    /**
     * Получить все отправки для конкретного теста.
     * 
     * @param testId идентификатор теста
     * @return список отправок для теста
     */
    List<StudentSubmission> getSubmissionsByTestId(Long testId);
    
    /**
     * Получить все отправки конкретного студента для конкретного теста.
     * 
     * @param studentId идентификатор студента
     * @param testId идентификатор теста
     * @return список отправок (может быть несколько попыток)
     */
    List<StudentSubmission> getSubmissionsByStudentIdAndTestId(Long studentId, Long testId);
    
    /**
     * Получить все непроверенные отправки.
     * 
     * @return список непроверенных отправок
     */
    List<StudentSubmission> getUnreviewedSubmissions();
    
    /**
     * Обновить оценку за отправку.
     * 
     * @param id идентификатор отправки
     * @param score новая оценка
     * @return обновленная отправка
     */
    StudentSubmission updateSubmissionScore(Long id, Integer score);
    
    /**
     * Отметить отправку как проверенную.
     * 
     * @param id идентификатор отправки
     * @param reviewed флаг проверки
     * @return обновленная отправка
     */
    StudentSubmission markSubmissionAsReviewed(Long id, boolean reviewed);
    
    /**
     * Удалить отправку.
     * 
     * @param id идентификатор отправки
     */
    void deleteSubmission(Long id);
    
    /**
     * Проверить, принадлежит ли отправка студенту.
     * 
     * @param submissionId идентификатор отправки
     * @param studentId идентификатор студента
     * @return true, если отправка принадлежит студенту
     */
    boolean isSubmissionBelongsToStudent(Long submissionId, Long studentId);
    
    /**
     * Рассчитать оценку за отправку на основе ответов студента.
     * 
     * @param submissionId идентификатор отправки
     * @return рассчитанная оценка
     */
    int calculateSubmissionScore(Long submissionId);
    
    /**
     * Получить последнюю отправку студента для теста.
     * 
     * @param studentId идентификатор студента
     * @param testId идентификатор теста
     * @return последняя отправка или пустой Optional, если отправок нет
     */
    Optional<StudentSubmission> getLatestSubmission(Long studentId, Long testId);

    /**
     * Завершить отправку теста.
     *
     * @param submissionId идентификатор отправки
     * @return обновленная отправка
     */
    StudentSubmission completeSubmission(Long submissionId);
}
