package com.example.course_app.service.courses;

import com.example.course_app.entity.Role;
import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.courses.CourseStatus;
import com.example.course_app.entity.enrollments.CourseEnrollment;
import com.example.course_app.repository.CourseEnrollmentRepository;
import com.example.course_app.repository.CourseRepository;
import com.example.course_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseEnrollmentServiceImplTest {

    @Mock
    private CourseEnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseEnrollmentServiceImpl courseEnrollmentService;

    private User student;
    private Course publicCourse;
    private Course privateCourse;
    private Course unpublishedCourse;
    private CourseEnrollment enrollment;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(1L);
        student.setEmail("student@test.com");
        student.setRole(Role.USER);

        publicCourse = new Course();
        publicCourse.setId(1L);
        publicCourse.setTitle("Public Course");
        publicCourse.setPublic(true);
        publicCourse.setStatus(CourseStatus.PUBLISHED);

        privateCourse = new Course();
        privateCourse.setId(2L);
        privateCourse.setTitle("Private Course");
        privateCourse.setPublic(false);
        privateCourse.setStatus(CourseStatus.PUBLISHED);

        unpublishedCourse = new Course();
        unpublishedCourse.setId(3L);
        unpublishedCourse.setTitle("Unpublished Course");
        unpublishedCourse.setPublic(true);
        unpublishedCourse.setStatus(CourseStatus.DRAFT);

        enrollment = new CourseEnrollment();
        enrollment.setId(1L);
        enrollment.setStudent(student);
        enrollment.setCourse(publicCourse);
    }

    @Test
    void enrollStudentToCourse_Success() {
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(courseRepository.findById(publicCourse.getId())).thenReturn(Optional.of(publicCourse));
        when(enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), publicCourse.getId()))
            .thenReturn(false);
        when(enrollmentRepository.save(any(CourseEnrollment.class))).thenReturn(enrollment);

        CourseEnrollment result = courseEnrollmentService.enrollStudentToCourse(student.getId(), publicCourse.getId());

        assertNotNull(result);
        assertEquals(student, result.getStudent());
        assertEquals(publicCourse, result.getCourse());
        verify(enrollmentRepository).save(any(CourseEnrollment.class));
    }

    @Test
    void enrollStudentToCourse_StudentNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            courseEnrollmentService.enrollStudentToCourse(99L, publicCourse.getId())
        );
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudentToCourse_CourseNotFound() {
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            courseEnrollmentService.enrollStudentToCourse(student.getId(), 99L)
        );
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudentToCourse_NonPublicCourse() {
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(courseRepository.findById(privateCourse.getId())).thenReturn(Optional.of(privateCourse));

        assertThrows(IllegalStateException.class, () ->
            courseEnrollmentService.enrollStudentToCourse(student.getId(), privateCourse.getId())
        );
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudentToCourse_UnpublishedCourse() {
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(courseRepository.findById(unpublishedCourse.getId())).thenReturn(Optional.of(unpublishedCourse));

        assertThrows(IllegalStateException.class, () ->
            courseEnrollmentService.enrollStudentToCourse(student.getId(), unpublishedCourse.getId())
        );
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudentToCourse_AlreadyEnrolled() {
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(courseRepository.findById(publicCourse.getId())).thenReturn(Optional.of(publicCourse));
        when(enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), publicCourse.getId()))
            .thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
            courseEnrollmentService.enrollStudentToCourse(student.getId(), publicCourse.getId())
        );
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void unenrollStudentFromCourse_Success() {
        when(enrollmentRepository.findByStudentIdAndCourseId(student.getId(), publicCourse.getId()))
            .thenReturn(Optional.of(enrollment));

        courseEnrollmentService.unenrollStudentFromCourse(student.getId(), publicCourse.getId());

        verify(enrollmentRepository).delete(enrollment);
    }

    @Test
    void unenrollStudentFromCourse_EnrollmentNotFound() {
        when(enrollmentRepository.findByStudentIdAndCourseId(student.getId(), publicCourse.getId()))
            .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () ->
            courseEnrollmentService.unenrollStudentFromCourse(student.getId(), publicCourse.getId())
        );
        verify(enrollmentRepository, never()).delete(any());
    }

    @Test
    void getEnrollmentsByStudentId_Success() {
        List<CourseEnrollment> enrollments = Arrays.asList(enrollment);
        when(enrollmentRepository.findByStudentId(student.getId())).thenReturn(enrollments);

        List<CourseEnrollment> result = courseEnrollmentService.getEnrollmentsByStudentId(student.getId());

        assertEquals(enrollments, result);
        assertEquals(1, result.size());
    }

    @Test
    void getEnrollmentsByCourseId_Success() {
        List<CourseEnrollment> enrollments = Arrays.asList(enrollment);
        when(enrollmentRepository.findByCourseId(publicCourse.getId())).thenReturn(enrollments);

        List<CourseEnrollment> result = courseEnrollmentService.getEnrollmentsByCourseId(publicCourse.getId());

        assertEquals(enrollments, result);
        assertEquals(1, result.size());
    }

    @Test
    void isStudentEnrolledInCourse_True() {
        when(enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), publicCourse.getId()))
            .thenReturn(true);

        boolean result = courseEnrollmentService.isStudentEnrolledInCourse(student.getId(), publicCourse.getId());

        assertTrue(result);
    }

    @Test
    void isStudentEnrolledInCourse_False() {
        when(enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), publicCourse.getId()))
            .thenReturn(false);

        boolean result = courseEnrollmentService.isStudentEnrolledInCourse(student.getId(), publicCourse.getId());

        assertFalse(result);
    }

    @Test
    void getEnrollmentCountForCourse_Success() {
        List<CourseEnrollment> enrollments = Arrays.asList(enrollment);
        when(enrollmentRepository.findByCourseId(publicCourse.getId())).thenReturn(enrollments);

        long result = courseEnrollmentService.getEnrollmentCountForCourse(publicCourse.getId());

        assertEquals(1, result);
    }
}
