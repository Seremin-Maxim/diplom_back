package com.example.course_app.service.courses;


import com.example.course_app.entity.Role;
import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.enrollments.CourseEnrollment;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.entity.tests.Test;
import com.example.course_app.repository.CourseEnrollmentRepository;
import com.example.course_app.repository.CourseRepository;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.repository.TestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseStatisticsServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseEnrollmentRepository enrollmentRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private TestRepository testRepository;

    @InjectMocks
    private CourseStatisticsServiceImpl courseStatisticsService;

    private User teacher;
    private Course course1;
    private Course course2;
    private Lesson lesson1;
    private Lesson lesson2;
    private Test testEntity1;
    private Test testEntity2;
    private CourseEnrollment enrollment1;
    private CourseEnrollment enrollment2;

    @BeforeEach
    void setUp() {
        teacher = new User();
        teacher.setId(1L);
        teacher.setEmail("teacher@test.com");
        teacher.setRole(Role.TEACHER);

        course1 = new Course();
        course1.setId(1L);
        course1.setTeacher(teacher);
        course1.setTitle("Course 1");

        course2 = new Course();
        course2.setId(2L);
        course2.setTeacher(teacher);
        course2.setTitle("Course 2");

        lesson1 = new Lesson();
        lesson1.setId(1L);
        lesson1.setCourse(course1);

        lesson2 = new Lesson();
        lesson2.setId(2L);
        lesson2.setCourse(course1);

        testEntity1 = new Test();
        testEntity1.setId(1L);
        testEntity1.setLesson(lesson1);

        testEntity2 = new Test();
        testEntity2.setId(2L);
        testEntity2.setLesson(lesson2);

        enrollment1 = new CourseEnrollment();
        enrollment1.setId(1L);
        enrollment1.setCourse(course1);

        enrollment2 = new CourseEnrollment();
        enrollment2.setId(2L);
        enrollment2.setCourse(course1);
    }

    @org.junit.jupiter.api.Test
    void getStudentCountForCourse_Success() {
        when(enrollmentRepository.findByCourseId(course1.getId()))
                .thenReturn(Arrays.asList(enrollment1, enrollment2));

        long result = courseStatisticsService.getStudentCountForCourse(course1.getId());

        assertEquals(2, result);
    }

    @org.junit.jupiter.api.Test
    void getLessonCountForCourse_Success() {
        when(lessonRepository.findByCourseId(course1.getId()))
                .thenReturn(Arrays.asList(lesson1, lesson2));

        long result = courseStatisticsService.getLessonCountForCourse(course1.getId());

        assertEquals(2, result);
    }

    @org.junit.jupiter.api.Test
    void getTestCountForCourse_Success() {
        when(lessonRepository.findByCourseId(course1.getId()))
                .thenReturn(Arrays.asList(lesson1, lesson2));
        when(testRepository.findByLessonId(lesson1.getId()))
                .thenReturn(List.of(testEntity1));
        when(testRepository.findByLessonId(lesson2.getId()))
                .thenReturn(List.of(testEntity2));

        long result = courseStatisticsService.getTestCountForCourse(course1.getId());

        assertEquals(2, result);
    }

    @org.junit.jupiter.api.Test
    void getCourseStatistics_Success() {
        when(enrollmentRepository.findByCourseId(course1.getId()))
                .thenReturn(Arrays.asList(enrollment1, enrollment2));
        when(lessonRepository.findByCourseId(course1.getId()))
                .thenReturn(Arrays.asList(lesson1, lesson2));
        when(testRepository.findByLessonId(lesson1.getId()))
                .thenReturn(List.of(testEntity1));
        when(testRepository.findByLessonId(lesson2.getId()))
                .thenReturn(List.of(testEntity2));

        Map<String, Long> statistics = courseStatisticsService.getCourseStatistics(course1.getId());

        assertEquals(2L, statistics.get("studentCount"));
        assertEquals(2L, statistics.get("lessonCount"));
        assertEquals(2L, statistics.get("testCount"));
    }

    @org.junit.jupiter.api.Test
    void getCourseCountForTeacher_Success() {
        when(courseRepository.findByTeacherId(teacher.getId()))
                .thenReturn(Arrays.asList(course1, course2));

        long result = courseStatisticsService.getCourseCountForTeacher(teacher.getId());

        assertEquals(2, result);
    }

    @org.junit.jupiter.api.Test
    void getTotalStudentCountForTeacher_Success() {
        when(courseRepository.findByTeacherId(teacher.getId()))
                .thenReturn(Arrays.asList(course1, course2));
        when(enrollmentRepository.findByCourseId(course1.getId()))
                .thenReturn(Arrays.asList(enrollment1, enrollment2));
        when(enrollmentRepository.findByCourseId(course2.getId()))
                .thenReturn(List.of(enrollment1)); // Один студент на втором курсе

        long result = courseStatisticsService.getTotalStudentCountForTeacher(teacher.getId());

        assertEquals(3, result); // 2 студента на первом курсе + 1 на втором
    }

    @org.junit.jupiter.api.Test
    void getTeacherStatistics_Success() {
        // Настраиваем моки для всех вызовов
        when(courseRepository.findByTeacherId(teacher.getId()))
                .thenReturn(Arrays.asList(course1, course2));
        
        // Для первого курса
        when(enrollmentRepository.findByCourseId(course1.getId()))
                .thenReturn(Arrays.asList(enrollment1, enrollment2));
        when(lessonRepository.findByCourseId(course1.getId()))
                .thenReturn(Arrays.asList(lesson1, lesson2));
        when(testRepository.findByLessonId(lesson1.getId()))
                .thenReturn(List.of(testEntity1));
        when(testRepository.findByLessonId(lesson2.getId()))
                .thenReturn(List.of(testEntity2));
        
        // Для второго курса
        when(enrollmentRepository.findByCourseId(course2.getId()))
                .thenReturn(List.of(enrollment1));
        when(lessonRepository.findByCourseId(course2.getId()))
                .thenReturn(List.of(lesson1));
        when(testRepository.findByLessonId(lesson1.getId()))
                .thenReturn(List.of(testEntity1));

        Map<String, Long> statistics = courseStatisticsService.getTeacherStatistics(teacher.getId());

        assertEquals(2L, statistics.get("courseCount")); // 2 курса
        assertEquals(3L, statistics.get("studentCount")); // 2 + 1 студент
        assertEquals(3L, statistics.get("lessonCount")); // 2 + 1 урок
        assertEquals(3L, statistics.get("testCount")); // 2 + 1 тест
    }
}