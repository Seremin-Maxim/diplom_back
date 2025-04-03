package com.example.course_app.service.lessons;

import com.example.course_app.entity.User;
import com.example.course_app.entity.enrollments.LessonEnrollment;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.repository.CourseEnrollmentRepository;
import com.example.course_app.repository.LessonEnrollmentRepository;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для управления зачислением студентов на уроки.
 */
@Service
public class LessonEnrollmentServiceImpl implements LessonEnrollmentService {

    private final LessonEnrollmentRepository lessonEnrollmentRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;

    @Autowired
    public LessonEnrollmentServiceImpl(
            LessonEnrollmentRepository lessonEnrollmentRepository,
            LessonRepository lessonRepository,
            UserRepository userRepository,
            CourseEnrollmentRepository courseEnrollmentRepository) {
        this.lessonEnrollmentRepository = lessonEnrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
        this.courseEnrollmentRepository = courseEnrollmentRepository;
    }

    @Override
    @Transactional
    public LessonEnrollment enrollStudentToLesson(Long studentId, Long lessonId) {
        // Проверяем, существует ли студент
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Студент с ID " + studentId + " не найден"));

        // Проверяем, существует ли урок
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Урок с ID " + lessonId + " не найден"));

        // Проверяем, зачислен ли студент на курс, к которому принадлежит урок
        Long courseId = lesson.getCourse().getId();
        if (!courseEnrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new IllegalStateException("Студент не зачислен на курс, к которому принадлежит этот урок");
        }

        // Проверяем, не зачислен ли студент уже на этот урок
        if (lessonEnrollmentRepository.existsByStudentIdAndLessonId(studentId, lessonId)) {
            throw new IllegalStateException("Студент уже зачислен на этот урок");
        }

        // Создаем новое зачисление
        LessonEnrollment enrollment = new LessonEnrollment();
        enrollment.setStudent(student);
        enrollment.setLesson(lesson);
        enrollment.setJoinedAt(LocalDateTime.now());
        enrollment.setCompleted(false);

        // Сохраняем зачисление
        return lessonEnrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional
    public LessonEnrollment completeLessonForStudent(Long studentId, Long lessonId) {
        // Находим зачисление
        LessonEnrollment enrollment = lessonEnrollmentRepository.findByStudentIdAndLessonId(studentId, lessonId)
                .orElseThrow(() -> new IllegalStateException("Зачисление не найдено"));

        // Отмечаем урок как завершенный
        enrollment.setCompleted(true);
        // Записываем время завершения через обновление записи

        // Сохраняем обновленное зачисление
        return lessonEnrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LessonEnrollment> getEnrollmentById(Long enrollmentId) {
        return lessonEnrollmentRepository.findById(enrollmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonEnrollment> getEnrollmentsByStudentId(Long studentId) {
        return lessonEnrollmentRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonEnrollment> getEnrollmentsByLessonId(Long lessonId) {
        return lessonEnrollmentRepository.findByLessonId(lessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonEnrollment> getCompletedLessonsByStudentId(Long studentId) {
        return lessonEnrollmentRepository.findByStudentIdAndCompletedTrue(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentEnrolledInLesson(Long studentId, Long lessonId) {
        return lessonEnrollmentRepository.existsByStudentIdAndLessonId(studentId, lessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasStudentCompletedLesson(Long studentId, Long lessonId) {
        Optional<LessonEnrollment> enrollment = lessonEnrollmentRepository.findByStudentIdAndLessonId(studentId, lessonId);
        return enrollment.isPresent() && enrollment.get().getCompleted();
    }

    @Override
    @Transactional(readOnly = true)
    public double getStudentProgressInCourse(Long studentId, Long courseId) {
        // Получаем все уроки курса
        List<Lesson> courseLessons = lessonRepository.findByCourseId(courseId);
        
        if (courseLessons.isEmpty()) {
            return 0.0;
        }
        
        // Получаем все завершенные уроки студента для этого курса
        int completedLessons = 0;
        
        for (Lesson lesson : courseLessons) {
            if (hasStudentCompletedLesson(studentId, lesson.getId())) {
                completedLessons++;
            }
        }
        
        // Вычисляем процент завершенных уроков
        return (double) completedLessons / courseLessons.size() * 100;
    }
    
    /**
     * Проверить, зачислен ли студент на курс.
     *
     * @param studentId идентификатор студента
     * @param courseId идентификатор курса
     * @return true, если студент зачислен на курс
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isStudentEnrolledInCourse(Long studentId, Long courseId) {
        return courseEnrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }
}
