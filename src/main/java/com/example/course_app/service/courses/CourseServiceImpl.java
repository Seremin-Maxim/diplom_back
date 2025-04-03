package com.example.course_app.service.courses;

import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.courses.CourseStatus;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.repository.CourseRepository;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.repository.UserRepository;
import com.example.course_app.service.lessons.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для работы с курсами.
 */
@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final LessonService lessonService;

    @Autowired
    public CourseServiceImpl(
            CourseRepository courseRepository, 
            UserRepository userRepository,
            LessonRepository lessonRepository,
            LessonService lessonService) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.lessonService = lessonService;
    }

    @Override
    @Transactional
    public Course createCourse(Course course, Long teacherId) {
        // Проверяем, существует ли курс с таким названием
        if (courseRepository.existsByTitle(course.getTitle())) {
            throw new IllegalArgumentException("Курс с названием '" + course.getTitle() + "' уже существует");
        }

        // Находим преподавателя
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Преподаватель с ID " + teacherId + " не найден"));

        // Устанавливаем преподавателя для курса
        course.setTeacher(teacher);
        
        // По умолчанию курс создается в статусе DRAFT
        course.setStatus(CourseStatus.DRAFT);

        // Сохраняем курс
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course updateCourse(Long id, Course courseDetails) {
        // Находим существующий курс с загрузкой учителя
        Course existingCourse = courseRepository.findCourseWithTeacherById(id)
                .orElseThrow(() -> new IllegalStateException("Курс с ID " + id + " не найден"));

        // Проверяем, существует ли другой курс с таким названием
        if (!existingCourse.getTitle().equals(courseDetails.getTitle()) &&
                courseRepository.existsByTitleAndIdNot(courseDetails.getTitle(), id)) {
            throw new IllegalArgumentException("Курс с названием '" + courseDetails.getTitle() + "' уже существует");
        }

        // Обновляем данные курса
        existingCourse.setTitle(courseDetails.getTitle());
        existingCourse.setDescription(courseDetails.getDescription());
        
        // Обновляем публичность, если она указана
        if (courseDetails.isPublic() != existingCourse.isPublic()) {
            existingCourse.setPublic(courseDetails.isPublic());
        }
        
        // Не обновляем статус через этот метод
        // Для этого есть отдельные методы

        // Сохраняем обновленный курс
        Course savedCourse = courseRepository.save(existingCourse);
        
        // Загружаем курс с учителем, чтобы избежать LazyInitializationException
        return courseRepository.findCourseWithTeacherById(savedCourse.getId())
                .orElseThrow(() -> new IllegalStateException("Не удалось загрузить обновленный курс"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Course> getCourseById(Long id) {
        // Используем JOIN FETCH для загрузки связей вместе с курсом
        return courseRepository.findCourseWithTeacherById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getCoursesByTeacherId(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getPublishedCourses() {
        return courseRepository.findByStatus(CourseStatus.PUBLISHED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getPublicCourses() {
        return courseRepository.findByStatusAndIsPublicTrue(CourseStatus.PUBLISHED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> searchCoursesByTitle(String title) {
        return courseRepository.findByTitleContainingIgnoreCase(title);
    }

    @Override
    @Transactional
    public Course changeCourseStatus(Long id, CourseStatus status) {
        // Находим существующий курс
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Курс с ID " + id + " не найден"));

        // Обновляем статус курса
        course.setStatus(status);

        // Сохраняем обновленный курс
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course changeCoursePublicity(Long id, boolean isPublic) {
        // Находим существующий курс
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Курс с ID " + id + " не найден"));

        // Обновляем публичность курса
        course.setPublic(isPublic);

        // Сохраняем обновленный курс
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        // Проверяем, существует ли курс
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Курс с ID " + id + " не найден"));
        
        // Получаем все уроки для курса
        List<Lesson> lessons = lessonRepository.findByCourseId(id);
        
        // Удаляем все уроки (и связанные с ними тесты, вопросы, ответы и отправки)
        if (!lessons.isEmpty()) {
            for (Lesson lesson : lessons) {
                lessonService.deleteLesson(lesson.getId());
            }
        }

        // Удаляем курс
        courseRepository.delete(course);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return courseRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCourseOwnedByTeacher(Long courseId, Long teacherId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            return course.getTeacher().getId().equals(teacherId);
        }
        
        return false;
    }
}
