package com.example.course_app.service.lessons;

import com.example.course_app.entity.enrollments.LessonEnrollment;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.repository.LessonEnrollmentRepository;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.repository.TestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonStatisticsServiceImplTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LessonEnrollmentRepository lessonEnrollmentRepository;

    @Mock
    private TestRepository testRepository;

    @InjectMocks
    private LessonStatisticsServiceImpl lessonStatisticsService;

    private Lesson lesson;
    private LessonEnrollment enrollment1;
    private LessonEnrollment enrollment2;

    @BeforeEach
    void setUp() {
        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("Test Lesson");

        enrollment1 = new LessonEnrollment();
        enrollment1.setId(1L);
        enrollment1.setLesson(lesson);
        enrollment1.setCompleted(true);

        enrollment2 = new LessonEnrollment();
        enrollment2.setId(2L);
        enrollment2.setLesson(lesson);
        enrollment2.setCompleted(false);
    }

    @Test
    void getStudentCountForLesson_Success() {
        when(lessonEnrollmentRepository.findByLessonId(lesson.getId()))
                .thenReturn(Arrays.asList(enrollment1, enrollment2));

        long result = lessonStatisticsService.getStudentCountForLesson(lesson.getId());

        assertEquals(2, result);
    }

    @Test
    void getCompletedStudentCountForLesson_Success() {
        when(lessonEnrollmentRepository.findByLessonId(lesson.getId()))
                .thenReturn(Arrays.asList(enrollment1, enrollment2));

        long result = lessonStatisticsService.getCompletedStudentCountForLesson(lesson.getId());

        assertEquals(1, result);
    }

    @Test
    void getCompletionRateForLesson_Success() {
        when(lessonEnrollmentRepository.findByLessonId(lesson.getId()))
                .thenReturn(Arrays.asList(enrollment1, enrollment2));

        double result = lessonStatisticsService.getCompletionRateForLesson(lesson.getId());

        assertEquals(50.0, result);
    }

    @Test
    void getLessonStatistics_Success() {
        when(lessonEnrollmentRepository.findByLessonId(lesson.getId()))
                .thenReturn(Arrays.asList(enrollment1, enrollment2));
        when(testRepository.findByLessonId(lesson.getId()))
                .thenReturn(Arrays.asList(new com.example.course_app.entity.tests.Test()));

        Map<String, Object> statistics = lessonStatisticsService.getLessonStatistics(lesson.getId());

        assertEquals(2L, statistics.get("studentCount"));
        assertEquals(1L, statistics.get("testCount"));
        assertEquals(1L, statistics.get("completedCount"));
        assertEquals(50.0, statistics.get("completionRate"));
    }
}