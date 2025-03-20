package com.example.course_app.service.lessons;

import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.repository.CourseRepository;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.repository.TestRepository;
import com.example.course_app.service.tests.TestService;
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
//import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
//import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class LessonServiceImplTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TestRepository testRepository;

    @Mock
    private TestService testService;

    @InjectMocks
    private LessonServiceImpl lessonService;

    private Course course;
    private Lesson lesson;
    private com.example.course_app.entity.tests.Test lessonTest;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setId(1L);
        course.setTitle("Test Course");

        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("Test Lesson");
        lesson.setCourse(course);
        lesson.setOrderNumber(1);
        lesson.setIsContent(true);

        lessonTest = new com.example.course_app.entity.tests.Test();
        lessonTest.setId(1L);
        lessonTest.setLesson(lesson);
    }

    @Test
    void createLesson_Success() {
        // Создаем новый урок без orderNumber
        Lesson newLesson = new Lesson();
        newLesson.setTitle("Test Lesson");
        newLesson.setContent("Test Content");
        newLesson.setOrderNumber(null); // Явно устанавливаем null
        
        // Настраиваем моки
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(lessonRepository.findMaxOrderNumberByCourseId(course.getId())).thenReturn(0);
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson savedLesson = invocation.getArgument(0);
            savedLesson.setId(1L);
            return savedLesson;
        });
    
        // Вызываем тестируемый метод
        Lesson result = lessonService.createLesson(newLesson, course.getId());
    
        // Проверяем результат
        assertNotNull(result);
        assertEquals("Test Lesson", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals(1, result.getOrderNumber());
        assertEquals(course, result.getCourse());
        
        // Проверяем, что все необходимые методы были вызваны
        verify(courseRepository).findById(course.getId());
        verify(lessonRepository).findMaxOrderNumberByCourseId(course.getId());
        verify(lessonRepository).save(any(Lesson.class));
    }
    
    @Test
    void createLesson_WithExistingOrderNumber_Success() {
        // Создаем новый урок с уже установленным orderNumber
        Lesson newLesson = new Lesson();
        newLesson.setTitle("Test Lesson");
        newLesson.setContent("Test Content");
        newLesson.setOrderNumber(5); // Устанавливаем конкретный orderNumber
    
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);
    
        Lesson result = lessonService.createLesson(newLesson, course.getId());
    
        assertNotNull(result);
        assertEquals(lesson.getTitle(), result.getTitle());
    
        verify(courseRepository).findById(course.getId());
        verify(lessonRepository, never()).findMaxOrderNumberByCourseId(any()); // Этот метод не должен вызываться
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    void getLessonById_Success() {
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.of(lesson));

        Optional<Lesson> result = lessonService.getLessonById(lesson.getId());

        assertTrue(result.isPresent());
        assertEquals(lesson.getTitle(), result.get().getTitle());
    }

    @Test
    void getLessonById_NotFound() {
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.empty());

        Optional<Lesson> result = lessonService.getLessonById(lesson.getId());

        assertFalse(result.isPresent());
    }

    @Test
    void getLessonsByCourseId_Success() {
        List<Lesson> lessons = Arrays.asList(lesson);
        when(lessonRepository.findByCourseId(course.getId())).thenReturn(lessons);

        List<Lesson> result = lessonService.getLessonsByCourseId(course.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(lesson.getTitle(), result.get(0).getTitle());
    }

    @Test
    void getContentLessonsByCourseId_Success() {
        List<Lesson> lessons = Arrays.asList(lesson);
        when(lessonRepository.findByCourseIdAndIsContentTrue(course.getId())).thenReturn(lessons);

        List<Lesson> result = lessonService.getContentLessonsByCourseId(course.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(lesson.getTitle(), result.get(0).getTitle());
    }

    @Test
    void changeLessonContentFlag_Success() {
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);

        Lesson result = lessonService.changeLessonContentFlag(lesson.getId(), true);

        assertNotNull(result);
        assertTrue(result.getIsContent());
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    void existsById_True() {
        when(lessonRepository.existsById(lesson.getId())).thenReturn(true);

        boolean result = lessonService.existsById(lesson.getId());

        assertTrue(result);
    }

    @Test
    void existsById_False() {
        when(lessonRepository.existsById(lesson.getId())).thenReturn(false);

        boolean result = lessonService.existsById(lesson.getId());

        assertFalse(result);
    }

    @Test
    void isLessonBelongsToCourse_False() {
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.empty());

        boolean result = lessonService.isLessonBelongsToCourse(lesson.getId(), course.getId());

        assertFalse(result);
    }

    @Test
    void getNextOrderNumber_Success() {
        when(lessonRepository.findMaxOrderNumberByCourseId(course.getId())).thenReturn(5);

        Integer result = lessonService.getNextOrderNumber(course.getId());

        assertEquals(6, result);
        verify(lessonRepository).findMaxOrderNumberByCourseId(course.getId());
    }

    @Test
    void updateLesson_NotFound() {
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> lessonService.updateLesson(lesson.getId(), new Lesson()));

        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void changeLessonOrder_NotFound() {
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> lessonService.changeLessonOrder(lesson.getId(), 2));

        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void changeLessonContentFlag_NotFound() {
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> lessonService.changeLessonContentFlag(lesson.getId(), true));

        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void deleteLesson_NotFound() {
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> lessonService.deleteLesson(lesson.getId()));

        verify(lessonRepository, never()).delete(any(Lesson.class));
    }
}