package com.example.course_app.service.courses;

import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.courses.CourseStatus;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.repository.CourseRepository;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.repository.UserRepository;
import com.example.course_app.service.lessons.LessonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LessonService lessonService;

    @InjectMocks
    private CourseServiceImpl courseService;

    private User teacher;
    private Course course;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        // Создаем тестовые данные
        teacher = new User();
        teacher.setId(1L);
        teacher.setEmail("teacher@example.com");
        teacher.setFirstName("Иван");
        teacher.setLastName("Преподавателев");

        course = new Course();
        course.setId(1L);
        course.setTitle("Программирование на Java");
        course.setDescription("Курс по основам Java");
        course.setTeacher(teacher);
        course.setStatus(CourseStatus.DRAFT);
        course.setPublic(false);

        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("Введение в Java");
        lesson.setCourse(course);
    }

    @Test
    @DisplayName("Создание курса с уникальным названием")
    void createCourse_WithUniqueName_ShouldCreateCourse() {
        // Arrange
        when(courseRepository.existsByTitle(anyString())).thenReturn(false);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(teacher));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        // Act
        Course result = courseService.createCourse(course, teacher.getId());

        // Assert
        assertNotNull(result);
        assertEquals("Программирование на Java", result.getTitle());
        assertEquals(CourseStatus.DRAFT, result.getStatus());
        assertEquals(teacher, result.getTeacher());
        
        // Verify
        verify(courseRepository).existsByTitle(course.getTitle());
        verify(userRepository).findById(teacher.getId());
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Создание курса с существующим названием должно выбросить исключение")
    void createCourse_WithDuplicateName_ShouldThrowException() {
        // Arrange
        when(courseRepository.existsByTitle(anyString())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.createCourse(course, teacher.getId());
        });
        
        // Проверяем сообщение об ошибке
        assertTrue(exception.getMessage().contains("уже существует"));
        
        // Verify
        verify(courseRepository).existsByTitle(course.getTitle());
        verify(userRepository, never()).findById(anyLong());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Создание курса с несуществующим преподавателем должно выбросить исключение")
    void createCourse_WithNonExistentTeacher_ShouldThrowException() {
        // Arrange
        when(courseRepository.existsByTitle(anyString())).thenReturn(false);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.createCourse(course, 999L);
        });
        
        // Проверяем сообщение об ошибке
        assertTrue(exception.getMessage().contains("не найден"));
        
        // Verify
        verify(courseRepository).existsByTitle(course.getTitle());
        verify(userRepository).findById(999L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Обновление курса с уникальным названием")
    void updateCourse_WithUniqueName_ShouldUpdateCourse() {
        // Arrange
        Course updatedCourse = new Course();
        updatedCourse.setTitle("Обновленный курс Java");
        updatedCourse.setDescription("Обновленное описание");
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
        when(courseRepository.existsByTitleAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        // Act
        Course result = courseService.updateCourse(1L, updatedCourse);

        // Assert
        assertNotNull(result);
        assertEquals("Обновленный курс Java", result.getTitle());
        assertEquals("Обновленное описание", result.getDescription());
        
        // Verify
        verify(courseRepository).findById(1L);
        verify(courseRepository).existsByTitleAndIdNot(updatedCourse.getTitle(), 1L);
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Обновление курса с существующим названием должно выбросить исключение")
    void updateCourse_WithDuplicateName_ShouldThrowException() {
        // Arrange
        Course updatedCourse = new Course();
        updatedCourse.setTitle("Другой курс");
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
        when(courseRepository.existsByTitleAndIdNot(anyString(), anyLong())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.updateCourse(1L, updatedCourse);
        });
        
        // Проверяем сообщение об ошибке
        assertTrue(exception.getMessage().contains("уже существует"));
        
        // Verify
        verify(courseRepository).findById(1L);
        verify(courseRepository).existsByTitleAndIdNot(updatedCourse.getTitle(), 1L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Обновление несуществующего курса должно выбросить исключение")
    void updateCourse_NonExistentCourse_ShouldThrowException() {
        // Arrange
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            courseService.updateCourse(999L, course);
        });
        
        // Проверяем сообщение об ошибке
        assertTrue(exception.getMessage().contains("не найден"));
        
        // Verify
        verify(courseRepository).findById(999L);
        verify(courseRepository, never()).existsByTitleAndIdNot(anyString(), anyLong());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Получение курса по ID")
    void getCourseById_ExistingCourse_ShouldReturnCourse() {
        // Arrange
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));

        // Act
        Optional<Course> result = courseService.getCourseById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(course, result.get());
        
        // Verify
        verify(courseRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение всех курсов")
    void getAllCourses_ShouldReturnAllCourses() {
        // Arrange
        Course course2 = new Course();
        course2.setId(2L);
        course2.setTitle("Python для начинающих");
        
        List<Course> courses = Arrays.asList(course, course2);
        when(courseRepository.findAll()).thenReturn(courses);

        // Act
        List<Course> result = courseService.getAllCourses();

        // Assert
        assertEquals(2, result.size());
        assertEquals(courses, result);
        
        // Verify
        verify(courseRepository).findAll();
    }

    @Test
    @DisplayName("Удаление курса должно удалить все связанные уроки")
    void deleteCourse_ShouldDeleteAllRelatedLessons() {
        // Arrange
        List<Lesson> lessons = Arrays.asList(lesson);
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
        when(lessonRepository.findByCourseId(anyLong())).thenReturn(lessons);
        doNothing().when(lessonService).deleteLesson(anyLong());

        // Act
        courseService.deleteCourse(1L);

        // Assert & Verify
        verify(courseRepository).findById(1L);
        verify(lessonRepository).findByCourseId(1L);
        verify(lessonService).deleteLesson(lesson.getId());
        verify(courseRepository).delete(course);
    }

    @Test
    @DisplayName("Удаление несуществующего курса должно выбросить исключение")
    void deleteCourse_NonExistentCourse_ShouldThrowException() {
        // Arrange
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            courseService.deleteCourse(999L);
        });
        
        // Проверяем сообщение об ошибке
        assertTrue(exception.getMessage().contains("не найден"));
        
        // Verify
        verify(courseRepository).findById(999L);
        verify(lessonRepository, never()).findByCourseId(anyLong());
        verify(lessonService, never()).deleteLesson(anyLong());
        verify(courseRepository, never()).delete(any(Course.class));
    }

    @Test
    @DisplayName("Изменение статуса курса")
    void changeCourseStatus_ShouldUpdateStatus() {
        // Arrange
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        // Act
        Course result = courseService.changeCourseStatus(1L, CourseStatus.PUBLISHED);

        // Assert
        assertEquals(CourseStatus.PUBLISHED, result.getStatus());
        
        // Verify
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Изменение публичности курса")
    void changeCoursePublicity_ShouldUpdatePublicity() {
        // Arrange
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        // Act
        Course result = courseService.changeCoursePublicity(1L, true);

        // Assert
        assertTrue(result.isPublic());
        
        // Verify
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Проверка принадлежности курса преподавателю")
    void isCourseOwnedByTeacher_ShouldReturnTrue_WhenCourseOwnedByTeacher() {
        // Arrange
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));

        // Act
        boolean result = courseService.isCourseOwnedByTeacher(1L, 1L);

        // Assert
        assertTrue(result);
        
        // Verify
        verify(courseRepository).findById(1L);
    }

    @Test
    @DisplayName("Проверка принадлежности курса другому преподавателю")
    void isCourseOwnedByTeacher_ShouldReturnFalse_WhenCourseNotOwnedByTeacher() {
        // Arrange
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));

        // Act
        boolean result = courseService.isCourseOwnedByTeacher(1L, 2L);

        // Assert
        assertFalse(result);
        
        // Verify
        verify(courseRepository).findById(1L);
    }
}
