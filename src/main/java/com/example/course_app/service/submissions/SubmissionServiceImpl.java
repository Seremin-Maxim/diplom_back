package com.example.course_app.service.submissions;

import com.example.course_app.entity.User;
import com.example.course_app.entity.submissions.StudentAnswer;
import com.example.course_app.entity.submissions.StudentSubmission;
import com.example.course_app.entity.tests.Test;
import com.example.course_app.repository.StudentAnswerRepository;
import com.example.course_app.repository.StudentSubmissionRepository;
import com.example.course_app.repository.TestRepository;
import com.example.course_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для работы с отправками ответов студентов на тесты.
 */
@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final StudentSubmissionRepository submissionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final UserRepository userRepository;
    private final TestRepository testRepository;

    @Autowired
    public SubmissionServiceImpl(
            StudentSubmissionRepository submissionRepository,
            StudentAnswerRepository studentAnswerRepository,
            UserRepository userRepository,
            TestRepository testRepository) {
        this.submissionRepository = submissionRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.userRepository = userRepository;
        this.testRepository = testRepository;
    }

    @Override
    @Transactional
    public StudentSubmission createSubmission(Long studentId, Long testId) {
        // Находим студента
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalStateException("Студент с ID " + studentId + " не найден"));

        // Находим тест
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalStateException("Тест с ID " + testId + " не найден"));

        // Создаем новую отправку
        StudentSubmission submission = new StudentSubmission();
        submission.setStudent(student);
        submission.setTest(test);
        submission.setStartTime(LocalDateTime.now()); // Устанавливаем время начала
        submission.setEndTime(null); // Конец теста пока не установлен
        submission.setScore(0); // Начальная оценка
        submission.setReviewed(false); // Не проверено

        // Сохраняем отправку
        return submissionRepository.save(submission);
    }

    // метод для завершения теста
    @Override
    @Transactional
    public StudentSubmission completeSubmission(Long submissionId) {
        StudentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalStateException("Отправка с ID " + submissionId + " не найдена"));

        submission.setEndTime(LocalDateTime.now());
        return submissionRepository.save(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudentSubmission> getSubmissionById(Long id) {
        return submissionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentSubmission> getSubmissionsByStudentId(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentSubmission> getSubmissionsByTestId(Long testId) {
        return submissionRepository.findByTestId(testId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentSubmission> getSubmissionsByStudentIdAndTestId(Long studentId, Long testId) {
        return submissionRepository.findByStudentIdAndTestId(studentId, testId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentSubmission> getUnreviewedSubmissions() {
        return submissionRepository.findByReviewedFalse();
    }

    @Override
    @Transactional
    public StudentSubmission updateSubmissionScore(Long id, Integer score) {
        // Находим существующую отправку
        StudentSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Отправка с ID " + id + " не найдена"));

        // Обновляем оценку
        submission.setScore(score);

        // Сохраняем обновленную отправку
        return submissionRepository.save(submission);
    }

    @Override
    @Transactional
    public StudentSubmission markSubmissionAsReviewed(Long id, boolean reviewed) {
        // Находим существующую отправку
        StudentSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Отправка с ID " + id + " не найдена"));

        // Отмечаем как проверенную
        submission.setReviewed(reviewed);

        // Сохраняем обновленную отправку
        return submissionRepository.save(submission);
    }

    @Override
    @Transactional
    public void deleteSubmission(Long id) {
        // Находим отправку
        StudentSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Отправка с ID " + id + " не найдена"));

        // Получаем все ответы для отправки
        List<StudentAnswer> answers = studentAnswerRepository.findBySubmissionId(id);

        // Удаляем все ответы
        if (!answers.isEmpty()) {
            studentAnswerRepository.deleteAll(answers);
        }

        // Удаляем отправку
        submissionRepository.delete(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSubmissionBelongsToStudent(Long submissionId, Long studentId) {
        // Находим отправку
        StudentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalStateException("Отправка с ID " + submissionId + " не найдена"));

        // Проверяем принадлежность отправки студенту
        return submission.getStudent().getId().equals(studentId);
    }

    @Override
    @Transactional
    public int calculateSubmissionScore(Long submissionId) {
        // Находим отправку
        StudentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalStateException("Отправка с ID " + submissionId + " не найдена"));

        // Получаем все правильные ответы для отправки
        List<StudentAnswer> correctAnswers = studentAnswerRepository.findBySubmissionIdAndIsCorrectTrue(submissionId);

        // Рассчитываем общую сумму баллов
        int totalPoints = 0;
        for (StudentAnswer answer : correctAnswers) {
            totalPoints += answer.getQuestion().getPoints();
        }

        // Обновляем оценку в отправке
        submission.setScore(totalPoints);
        submissionRepository.save(submission);

        return totalPoints;
    }

    @Override
    @Transactional
    public Optional<StudentSubmission> getLatestSubmission(Long studentId, Long testId) {
        // Получаем все отправки студента для теста
        List<StudentSubmission> submissions = submissionRepository.findByStudentIdAndTestId(studentId, testId);

        // Если отправок нет, возвращаем пустой Optional
        if (submissions.isEmpty()) {
            return Optional.empty();
        }

        // Находим последнюю отправку по времени
        return submissions.stream()
                .max(Comparator.comparing(StudentSubmission::getCreatedAt));
    }
}
