package com.example.course_app.service.tests;

import com.example.course_app.dto.TestAccessDTO;
import com.example.course_app.entity.submissions.StudentSubmission;
import com.example.course_app.entity.tests.Test;
import com.example.course_app.repository.TestRepository;
import com.example.course_app.service.lessons.LessonEnrollmentService;
import com.example.course_app.service.submissions.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация сервиса для управления доступом к тестам.
 */
@Service
public class TestAccessServiceImpl implements TestAccessService {

    private final TestRepository testRepository;
    private final LessonEnrollmentService lessonEnrollmentService;
    private final SubmissionService submissionService;
    
    // Хранилище токенов доступа к тестам (в реальном приложении лучше использовать Redis или другое хранилище)
    private final Map<String, TestAccessDTO> accessTokensMap = new ConcurrentHashMap<>();

    /**
     * Конструктор сервиса.
     *
     * @param testRepository репозиторий тестов
     * @param lessonEnrollmentService сервис для управления записями на уроки
     * @param submissionService сервис для работы с отправками ответов
     */
    @Autowired
    public TestAccessServiceImpl(
            TestRepository testRepository,
            LessonEnrollmentService lessonEnrollmentService,
            SubmissionService submissionService) {
        this.testRepository = testRepository;
        this.lessonEnrollmentService = lessonEnrollmentService;
        this.submissionService = submissionService;
    }

    /**
     * Получить список доступных тестов для студента в уроке.
     *
     * @param studentId идентификатор студента
     * @param lessonId идентификатор урока
     * @return список доступных тестов
     */
    @Override
    @Transactional(readOnly = true)
    public List<Test> getAvailableTestsForStudent(Long studentId, Long lessonId) {
        // Проверяем, записан ли студент на урок
        if (!lessonEnrollmentService.isStudentEnrolledInLesson(studentId, lessonId)) {
            return Collections.emptyList();
        }
        
        // Получаем список тестов урока
        return testRepository.findByLessonId(lessonId);
    }

    /**
     * Проверить, имеет ли студент доступ к тесту.
     *
     * @param studentId идентификатор студента
     * @param testId идентификатор теста
     * @return true, если студент имеет доступ к тесту
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasStudentAccessToTest(Long studentId, Long testId) {
        // Получаем тест
        Optional<Test> testOpt = testRepository.findById(testId);
        if (testOpt.isEmpty()) {
            return false;
        }
        
        Test test = testOpt.get();
        Long lessonId = test.getLesson().getId();
        
        // Проверяем, записан ли студент на урок
        return lessonEnrollmentService.isStudentEnrolledInLesson(studentId, lessonId);
    }

    /**
     * Проверить, отправил ли студент ответы на тест.
     *
     * @param studentId идентификатор студента
     * @param testId идентификатор теста
     * @return true, если студент отправил ответы на тест
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasStudentSubmittedTest(Long studentId, Long testId) {
        // Получаем список отправок студента для теста
        List<StudentSubmission> submissions = submissionService.getSubmissionsByStudentIdAndTestId(studentId, testId);
        
        // Проверяем, есть ли завершенные отправки
        return submissions.stream()
                .anyMatch(submission -> submission.getEndTime() != null);
    }

    /**
     * Получить оценку студента за тест.
     *
     * @param studentId идентификатор студента
     * @param testId идентификатор теста
     * @return оценка студента за тест или null, если тест не пройден
     */
    @Override
    @Transactional(readOnly = true)
    public Double getStudentTestScore(Long studentId, Long testId) {
        // Получаем последнюю отправку студента для теста
        Optional<StudentSubmission> submissionOpt = submissionService.getLatestSubmission(studentId, testId);
        
        // Если отправки нет или она не завершена, возвращаем null
        if (submissionOpt.isEmpty() || submissionOpt.get().getEndTime() == null) {
            return null;
        }
        
        // Возвращаем оценку
        StudentSubmission submission = submissionOpt.get();
        return submission.getScore() != null ? submission.getScore().doubleValue() : null;
    }

    /**
     * Создать токен доступа к тесту для студента.
     *
     * @param studentId идентификатор студента
     * @param test тест
     * @return DTO с информацией о доступе к тесту
     */
    @Override
    public TestAccessDTO createTestAccessToken(Long studentId, Test test) {
        // Проверяем, отправил ли студент ответы на тест
        boolean hasSubmitted = hasStudentSubmittedTest(studentId, test.getId());
        Double score = null;
        
        if (hasSubmitted) {
            // Если отправил, получаем оценку
            score = getStudentTestScore(studentId, test.getId());
        }
        
        // Генерируем уникальный токен доступа
        String accessToken = UUID.randomUUID().toString();
        
        // Создаем DTO доступа к тесту
        TestAccessDTO accessDTO = new TestAccessDTO();
        accessDTO.setTestId(test.getId());
        accessDTO.setTestTitle(test.getTitle());
        accessDTO.setAccessToken(accessToken);
        accessDTO.setExpiresAt(LocalDateTime.now().plusDays(1)); // Токен действует 1 день
        accessDTO.setAccessUrl("/api/tests/access/" + test.getId() + "?token=" + accessToken);
        accessDTO.setCompleted(hasSubmitted);
        accessDTO.setScore(score);
        
        // Сохраняем токен в хранилище
        accessTokensMap.put(accessToken, accessDTO);
        
        return accessDTO;
    }

    /**
     * Проверить валидность токена доступа к тесту.
     *
     * @param token токен доступа
     * @param testId идентификатор теста
     * @return DTO с информацией о доступе к тесту или пустой Optional, если токен недействителен
     */
    @Override
    public Optional<TestAccessDTO> validateTestAccessToken(String token, Long testId) {
        // Получаем DTO доступа к тесту по токену
        TestAccessDTO accessDTO = accessTokensMap.get(token);
        
        // Проверяем, существует ли токен и соответствует ли он тесту
        if (accessDTO == null || !accessDTO.getTestId().equals(testId)) {
            return Optional.empty();
        }
        
        // Проверяем срок действия токена
        if (accessDTO.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Если срок действия истек, удаляем токен из хранилища
            accessTokensMap.remove(token);
            return Optional.empty();
        }
        
        return Optional.of(accessDTO);
    }
}
