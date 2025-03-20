package com.example.course_app.service.lessons;

import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.entity.enrollments.LessonEnrollment;
import com.example.course_app.repository.LessonEnrollmentRepository;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для получения статистики по урокам.
 */
@Service
public class LessonStatisticsServiceImpl implements LessonStatisticsService {

    private final LessonRepository lessonRepository;
    private final LessonEnrollmentRepository lessonEnrollmentRepository;
    private final TestRepository testRepository;

    @Autowired
    public LessonStatisticsServiceImpl(
            LessonRepository lessonRepository,
            LessonEnrollmentRepository lessonEnrollmentRepository,
            TestRepository testRepository) {
        this.lessonRepository = lessonRepository;
        this.lessonEnrollmentRepository = lessonEnrollmentRepository;
        this.testRepository = testRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public long getStudentCountForLesson(Long lessonId) {
        return lessonEnrollmentRepository.findByLessonId(lessonId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTestCountForLesson(Long lessonId) {
        return testRepository.findByLessonId(lessonId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long getCompletedStudentCountForLesson(Long lessonId) {
        return lessonEnrollmentRepository.findByLessonId(lessonId).stream()
                .filter(LessonEnrollment::getCompleted)
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public double getCompletionRateForLesson(Long lessonId) {
        long totalStudents = getStudentCountForLesson(lessonId);
        
        if (totalStudents == 0) {
            return 0.0;
        }
        
        long completedStudents = getCompletedStudentCountForLesson(lessonId);
        
        return (double) completedStudents / totalStudents * 100;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getLessonStatistics(Long lessonId) {
        Map<String, Object> statistics = new HashMap<>();
        
        long studentCount = getStudentCountForLesson(lessonId);
        long testCount = getTestCountForLesson(lessonId);
        long completedCount = getCompletedStudentCountForLesson(lessonId);
        double completionRate = getCompletionRateForLesson(lessonId);
        
        statistics.put("studentCount", studentCount);
        statistics.put("testCount", testCount);
        statistics.put("completedCount", completedCount);
        statistics.put("completionRate", completionRate);
        
        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Map<String, Object>> getLessonStatisticsForCourse(Long courseId) {
        // Получаем все уроки курса
        List<Lesson> lessons = lessonRepository.findByCourseId(courseId);
        
        // Создаем карту статистики для каждого урока
        return lessons.stream()
                .collect(Collectors.toMap(
                        Lesson::getId,
                        lesson -> getLessonStatistics(lesson.getId())
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public double getAverageCompletionRateForCourse(Long courseId) {
        // Получаем все уроки курса
        List<Lesson> lessons = lessonRepository.findByCourseId(courseId);
        
        if (lessons.isEmpty()) {
            return 0.0;
        }
        
        // Вычисляем среднее значение процента завершения
        double totalCompletionRate = lessons.stream()
                .mapToDouble(lesson -> getCompletionRateForLesson(lesson.getId()))
                .sum();
        
        return totalCompletionRate / lessons.size();
    }
}
