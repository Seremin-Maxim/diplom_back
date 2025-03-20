package com.example.course_app.service.courses;

import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.enrollments.CourseEnrollment;
import com.example.course_app.entity.courses.CourseStatus;
import com.example.course_app.repository.CourseEnrollmentRepository;
import com.example.course_app.repository.CourseRepository;
import com.example.course_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для управления зачислением студентов на курсы.
 */
@Service
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {

    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Autowired
    public CourseEnrollmentServiceImpl(
            CourseEnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CourseEnrollment enrollStudentToCourse(Long studentId, Long courseId) {
        // Проверяем, существует ли студент
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Студент с ID " + studentId + " не найден"));

        // Проверяем, существует ли курс
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Курс с ID " + courseId + " не найден"));

        // Проверяем, опубликован ли курс
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalStateException("Нельзя зачислиться на неопубликованный курс");
        }

        // Проверяем, является ли курс публичным или студент имеет специальный доступ
        if (!course.isPublic()) {
            // Здесь можно добавить проверку на специальный доступ, если такая функциональность будет добавлена
            throw new IllegalStateException("Курс не является публичным");
        }

        // Проверяем, не зачислен ли студент уже на этот курс
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new IllegalStateException("Студент уже зачислен на этот курс");
        }

        // Создаем новое зачисление
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setJoinedAt(LocalDateTime.now());
        enrollment.setCompleted(false);

        // Сохраняем зачисление
        return enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional
    public void unenrollStudentFromCourse(Long studentId, Long courseId) {
        // Находим зачисление
        CourseEnrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new IllegalStateException("Зачисление не найдено"));

        // Удаляем зачисление
        enrollmentRepository.delete(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CourseEnrollment> getEnrollmentById(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseEnrollment> getEnrollmentsByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseEnrollment> getEnrollmentsByCourseId(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentEnrolledInCourse(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getEnrollmentCountForCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).size();
    }
}
