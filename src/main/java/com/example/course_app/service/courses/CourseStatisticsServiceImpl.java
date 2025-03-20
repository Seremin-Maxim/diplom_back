package com.example.course_app.service.courses;

import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.repository.CourseEnrollmentRepository;
import com.example.course_app.repository.CourseRepository;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация сервиса для получения статистики по курсам.
 */
@Service
public class CourseStatisticsServiceImpl implements CourseStatisticsService {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final TestRepository testRepository;

    @Autowired
    public CourseStatisticsServiceImpl(
            CourseRepository courseRepository,
            CourseEnrollmentRepository enrollmentRepository,
            LessonRepository lessonRepository,
            TestRepository testRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.testRepository = testRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public long getStudentCountForCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long getLessonCountForCourse(Long courseId) {
        return lessonRepository.findByCourseId(courseId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTestCountForCourse(Long courseId) {
        // Получаем все уроки курса
        List<Lesson> lessons = lessonRepository.findByCourseId(courseId);
        
        // Считаем количество тестов для всех уроков
        return lessons.stream()
                .mapToLong(lesson -> testRepository.findByLessonId(lesson.getId()).size())
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getCourseStatistics(Long courseId) {
        Map<String, Long> statistics = new HashMap<>();
        
        statistics.put("studentCount", getStudentCountForCourse(courseId));
        statistics.put("lessonCount", getLessonCountForCourse(courseId));
        statistics.put("testCount", getTestCountForCourse(courseId));
        
        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public long getCourseCountForTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalStudentCountForTeacher(Long teacherId) {
        // Получаем все курсы преподавателя
        return courseRepository.findByTeacherId(teacherId).stream()
                .mapToLong(course -> enrollmentRepository.findByCourseId(course.getId()).size())
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getTeacherStatistics(Long teacherId) {
        Map<String, Long> statistics = new HashMap<>();
        
        long courseCount = getCourseCountForTeacher(teacherId);
        statistics.put("courseCount", courseCount);
        statistics.put("studentCount", getTotalStudentCountForTeacher(teacherId));
        
        // Получаем количество уроков и тестов для всех курсов преподавателя
        long lessonCount = 0;
        long testCount = 0;
        
        for (var course : courseRepository.findByTeacherId(teacherId)) {
            lessonCount += getLessonCountForCourse(course.getId());
            testCount += getTestCountForCourse(course.getId());
        }
        
        statistics.put("lessonCount", lessonCount);
        statistics.put("testCount", testCount);
        
        return statistics;
    }
}
