package com.example.course_app.service.tests;

import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.entity.questions.Question;
import com.example.course_app.entity.submissions.StudentSubmission;
import com.example.course_app.entity.tests.Test;
import com.example.course_app.entity.tests.TestType;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.repository.QuestionRepository;
import com.example.course_app.repository.StudentSubmissionRepository;
import com.example.course_app.repository.TestRepository;
import com.example.course_app.service.questions.QuestionService;
import com.example.course_app.service.submissions.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для работы с тестами.
 */
@Service
public class TestServiceImpl implements TestService {

    private final TestRepository testRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final StudentSubmissionRepository studentSubmissionRepository;
    private final QuestionService questionService;
    private final SubmissionService submissionService;

    @Autowired
    public TestServiceImpl(
            TestRepository testRepository,
            LessonRepository lessonRepository,
            QuestionRepository questionRepository,
            StudentSubmissionRepository studentSubmissionRepository,
            QuestionService questionService,
            SubmissionService submissionService) {
        this.testRepository = testRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
        this.studentSubmissionRepository = studentSubmissionRepository;
        this.questionService = questionService;
        this.submissionService = submissionService;
    }

    @Override
    @Transactional
    public Test createTest(Long lessonId, String title, TestType type, boolean requiresManualCheck) {
        // Находим урок
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalStateException("Урок с ID " + lessonId + " не найден"));

        // Создаем новый тест
        Test test = new Test();
        test.setLesson(lesson);
        test.setTitle(title);
        test.setType(type);
        test.setRequiresManualCheck(requiresManualCheck);

        // Сохраняем тест
        return testRepository.save(test);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Test> getTestById(Long id) {
        return testRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Test> getTestsByLessonId(Long lessonId) {
        return testRepository.findByLessonId(lessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Test> getTestsByType(TestType type) {
        return testRepository.findByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Test> getTestsByLessonIdAndType(Long lessonId, TestType type) {
        return testRepository.findByLessonIdAndType(lessonId, type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Test> getTestsRequiringManualCheck() {
        return testRepository.findByRequiresManualCheckTrue();
    }

    @Override
    @Transactional
    public Test updateTest(Long id, String title, TestType type, boolean requiresManualCheck) {
        // Находим существующий тест
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Тест с ID " + id + " не найден"));

        // Обновляем информацию о тесте
        test.setTitle(title);
        test.setType(type);
        test.setRequiresManualCheck(requiresManualCheck);

        // Сохраняем обновленный тест
        return testRepository.save(test);
    }

    @Override
    @Transactional
    public void deleteTest(Long id) {
        // Находим тест
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Тест с ID " + id + " не найден"));
        
        // Получаем все отправки студентов для этого теста
        List<StudentSubmission> submissions = studentSubmissionRepository.findByTestId(id);
        
        // Удаляем все отправки и связанные с ними ответы студентов
        if (!submissions.isEmpty()) {
            for (StudentSubmission submission : submissions) {
                submissionService.deleteSubmission(submission.getId());
            }
        }
        
        // Получаем все вопросы для теста
        List<Question> questions = questionRepository.findByTestId(id);
        
        // Удаляем все вопросы (и связанные с ними ответы)
        if (!questions.isEmpty()) {
            for (Question question : questions) {
                questionService.deleteQuestion(question.getId());
            }
        }
        
        // Удаляем тест
        testRepository.delete(test);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTestBelongsToLesson(Long testId, Long lessonId) {
        // Находим тест
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalStateException("Тест с ID " + testId + " не найден"));

        // Проверяем принадлежность теста уроку
        return test.getLesson().getId().equals(lessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTestRequiresManualCheck(Long testId) {
        // Находим тест
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalStateException("Тест с ID " + testId + " не найден"));

        // Проверяем, требует ли тест ручной проверки
        return test.isRequiresManualCheck();
    }

    @Override
    @Transactional(readOnly = true)
    public long getQuestionCount(Long testId) {
        // Проверяем существование теста
        if (!testRepository.existsById(testId)) {
            throw new IllegalStateException("Тест с ID " + testId + " не найден");
        }

        // Получаем количество вопросов в тесте
        return questionRepository.countByTestId(testId);
    }

    @Override
    @Transactional(readOnly = true)
    public int getMaxPoints(Long testId) {
        // Проверяем существование теста
        if (!testRepository.existsById(testId)) {
            throw new IllegalStateException("Тест с ID " + testId + " не найден");
        }

        // Получаем сумму баллов за все вопросы в тесте
        Integer totalPoints = questionRepository.sumPointsByTestId(testId);
        
        // Если сумма не определена (нет вопросов), возвращаем 0
        return totalPoints != null ? totalPoints : 0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isTestCreatedByUser(Long testId, Long userId) {
        // Находим тест
        Optional<Test> testOpt = testRepository.findById(testId);
        if (testOpt.isEmpty()) {
            return false;
        }
        
        Test test = testOpt.get();
        Lesson lesson = test.getLesson();
        
        // Проверяем, является ли пользователь создателем курса
        return lesson.getCourse().getTeacher().getId().equals(userId);
    }
}
