package com.example.course_app.service.lessons;

import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.enrollments.LessonEnrollment;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.repository.CourseEnrollmentRepository;
import com.example.course_app.repository.LessonEnrollmentRepository;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonEnrollmentServiceImplTest {

    @Mock
    private LessonEnrollmentRepository lessonEnrollmentRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseEnrollmentRepository courseEnrollmentRepository;

    @InjectMocks
    private LessonEnrollmentServiceImpl lessonEnrollmentService;

    private User student;
    private Course course;
    private Lesson lesson;
    private LessonEnrollment enrollment;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(1L);

        course = new Course();
        course.setId(1L);

        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setCourse(course);

        enrollment = new LessonEnrollment();
        enrollment.setId(1L);
        enrollment.setStudent(student);
        enrollment.setLesson(lesson);
        enrollment.setCompleted(false);
    }

    @Test
    void enrollStudentToLesson_Success() {
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.of(lesson));
        when(courseEnrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId()))
                .thenReturn(true);
        when(lessonEnrollmentRepository.existsByStudentIdAndLessonId(student.getId(), lesson.getId()))
                .thenReturn(false);
        when(lessonEnrollmentRepository.save(any(LessonEnrollment.class))).thenReturn(enrollment);

        LessonEnrollment result = lessonEnrollmentService.enrollStudentToLesson(student.getId(), lesson.getId());

        assertNotNull(result);
        assertEquals(student.getId(), result.getStudent().getId());
        assertEquals(lesson.getId(), result.getLesson().getId());
    }

    @Test
    void completeLessonForStudent_Success() {
        when(lessonEnrollmentRepository.findByStudentIdAndLessonId(student.getId(), lesson.getId()))
                .thenReturn(Optional.of(enrollment));
        when(lessonEnrollmentRepository.save(any(LessonEnrollment.class))).thenReturn(enrollment);

        LessonEnrollment result = lessonEnrollmentService.completeLessonForStudent(student.getId(), lesson.getId());

        assertNotNull(result);
        assertTrue(result.getCompleted());
    }

    @Test
    void getStudentProgressInCourse_Success() {
        Lesson lesson2 = new Lesson();
        lesson2.setId(2L);
        lesson2.setCourse(course);

        when(lessonRepository.findByCourseId(course.getId()))
                .thenReturn(Arrays.asList(lesson, lesson2));
        when(lessonEnrollmentRepository.findByStudentIdAndLessonId(student.getId(), lesson.getId()))
                .thenReturn(Optional.of(enrollment));
        when(lessonEnrollmentRepository.findByStudentIdAndLessonId(student.getId(), lesson2.getId()))
                .thenReturn(Optional.empty());

        double progress = lessonEnrollmentService.getStudentProgressInCourse(student.getId(), course.getId());

        assertEquals(0.0, progress);
    }
}
