package com.example.course_app.service.tests;

import com.example.course_app.entity.submissions.StudentSubmission;
import com.example.course_app.entity.tests.Test;
import com.example.course_app.repository.StudentSubmissionRepository;
import com.example.course_app.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TestStatisticsServiceImpl implements TestStatisticsService {

    private final TestRepository testRepository;
    private final StudentSubmissionRepository submissionRepository;

    @Autowired
    public TestStatisticsServiceImpl(
            TestRepository testRepository,
            StudentSubmissionRepository submissionRepository) {
        this.testRepository = testRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public long getStudentCountForTest(Long testId) {
        validateTestExists(testId);
        return submissionRepository.countDistinctStudentsByTestId(testId);
    }

    @Override
    @Transactional(readOnly = true)
    public double getAverageScoreForTest(Long testId) {
        validateTestExists(testId);
        Double avgScore = submissionRepository.findAverageScoreByTestId(testId);
        return avgScore != null ? avgScore : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public double getSuccessRateForTest(Long testId, double threshold) {
        validateTestExists(testId);
        if (threshold < 0 || threshold > 100) {
            throw new IllegalArgumentException("Пороговое значение должно быть между 0 и 100");
        }

        Test test = testRepository.findById(testId).get();
        int maxPoints = test.getQuestions().stream()
                .mapToInt(q -> q.getPoints())
                .sum();
        
        double thresholdScore = (threshold / 100.0) * maxPoints;
        
        List<StudentSubmission> submissions = submissionRepository.findByTestId(testId);
        if (submissions.isEmpty()) {
            return 0.0;
        }

        long successfulSubmissions = submissions.stream()
                .filter(s -> s.getScore() >= thresholdScore)
                .count();

        return (double) successfulSubmissions / submissions.size() * 100;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getScoreDistributionForTest(Long testId) {
        validateTestExists(testId);
        
        List<StudentSubmission> submissions = submissionRepository.findByTestId(testId);
        Map<String, Long> distribution = new HashMap<>();
        
        // Инициализируем диапазоны
        distribution.put("0-20", 0L);
        distribution.put("21-40", 0L);
        distribution.put("41-60", 0L);
        distribution.put("61-80", 0L);
        distribution.put("81-100", 0L);

        Test test = testRepository.findById(testId).get();
        int maxPoints = test.getQuestions().stream()
                .mapToInt(q -> q.getPoints())
                .sum();

        for (StudentSubmission submission : submissions) {
            double percentageScore = (submission.getScore() / (double) maxPoints) * 100;
            String range = getScoreRange(percentageScore);
            distribution.put(range, distribution.get(range) + 1);
        }

        return distribution;
    }

    @Override
    @Transactional(readOnly = true)
    public double getAverageCompletionTimeForTest(Long testId) {
        validateTestExists(testId);
        
        List<StudentSubmission> submissions = submissionRepository.findCompletedByTestId(testId);
        if (submissions.isEmpty()) {
            return 0.0;
        }

        double totalMinutes = submissions.stream()
                .mapToLong(s -> {
                    if (s.getStartTime() != null && s.getEndTime() != null) {
                        return Duration.between(s.getStartTime(), s.getEndTime()).toMinutes();
                    }
                    return 0;
                })
                .average()
                .orElse(0.0);

        return totalMinutes;
    }

    @Override
    @Transactional(readOnly = true)
    public long getIncompleteAttemptsCountForTest(Long testId) {
        validateTestExists(testId);
        return submissionRepository.countIncompleteByTestId(testId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getQuestionStatistics(Long questionId) {
        Map<String, Object> statistics = new HashMap<>();
        
        Double averageScore = submissionRepository.findAverageScoreForQuestion(questionId);
        Long totalAttempts = submissionRepository.countAttemptsByQuestionId(questionId);
        Long correctAnswers = submissionRepository.countCorrectAnswersByQuestionId(questionId);
        
        statistics.put("averageScore", averageScore != null ? averageScore : 0.0);
        statistics.put("totalAttempts", totalAttempts != null ? totalAttempts : 0L);
        statistics.put("correctAnswers", correctAnswers != null ? correctAnswers : 0L);
        statistics.put("successRate", totalAttempts != null && totalAttempts > 0 ? 
                (double) (correctAnswers != null ? correctAnswers : 0L) / totalAttempts * 100 : 0.0);
        
        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTestStatistics(Long testId) {
        validateTestExists(testId);
        
        Map<String, Object> statistics = new HashMap<>();
        
        statistics.put("studentCount", getStudentCountForTest(testId));
        statistics.put("averageScore", getAverageScoreForTest(testId));
        statistics.put("successRate", getSuccessRateForTest(testId, 60.0)); // Используем 60% как стандартный порог
        statistics.put("scoreDistribution", getScoreDistributionForTest(testId));
        statistics.put("averageCompletionTime", getAverageCompletionTimeForTest(testId));
        statistics.put("incompleteAttempts", getIncompleteAttemptsCountForTest(testId));
        
        return statistics;
    }

    private void validateTestExists(Long testId) {
        if (!testRepository.existsById(testId)) {
            throw new IllegalStateException("Тест с ID " + testId + " не найден");
        }
    }

    private String getScoreRange(double score) {
        if (score <= 20) return "0-20";
        if (score <= 40) return "21-40";
        if (score <= 60) return "41-60";
        if (score <= 80) return "61-80";
        return "81-100";
    }
}