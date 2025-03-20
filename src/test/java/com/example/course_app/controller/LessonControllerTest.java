package com.example.course_app.controller;

import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.service.courses.CourseService;
import com.example.course_app.service.lessons.LessonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;



import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LessonControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private LessonService lessonService;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private LessonController lessonControllerOriginal;
    
    private LessonController lessonController;

    private User teacher;
    private Course course;
    private Lesson lesson;
    private List<Lesson> lessons;


    @BeforeEach
    void setUp() {
        // Настраиваем ObjectMapper для игнорирования проблемных полей
        objectMapper = new ObjectMapper();
        objectMapper.addMixIn(User.class, UserMixIn.class);
        
        // Создаем тестовые данные
        teacher = new User();
        teacher.setId(1L);
        teacher.setEmail("teacher@example.com");
        teacher.setFirstName("Иван");
        teacher.setLastName("Учителев");
        
        course = new Course();
        course.setId(1L);
        course.setTitle("Тестовый курс");
        course.setDescription("Описание тестового курса");
        
        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("Тестовый урок");
        lesson.setContent("Содержание тестового урока");
        lesson.setCourse(course);
        lesson.setOrderNumber(1);
        lesson.setIsContent(true);
        
        Lesson lesson2 = new Lesson();
        lesson2.setId(2L);
        lesson2.setTitle("Тестовый урок 2");
        lesson2.setContent("Содержание тестового урока 2");
        lesson2.setCourse(course);
        lesson2.setOrderNumber(2);
        lesson2.setIsContent(false);
        
        lessons = Arrays.asList(lesson, lesson2);
        
        // Создаем шпиона для контроллера, чтобы мочь мокировать метод getUserIdFromAuthentication
        lessonController = spy(lessonControllerOriginal);
        
        // Мокируем вызовы методов для аутентификации в тестах
        lenient().when(courseService.isCourseOwnedByTeacher(anyLong(), eq(1L))).thenReturn(true);
        
        // Мокируем метод getUserIdFromAuthentication, чтобы он возвращал ID пользователя
        doReturn(1L).when(lessonController).getUserIdFromAuthentication(any());
        
        // Настраиваем MockMvc
        mockMvc = MockMvcBuilders
                .standaloneSetup(lessonController)
                .build();
    }

    @Test
    @DisplayName("Получение всех уроков курса")
    void getLessonsByCourseId_ShouldReturnAllLessons() throws Exception {
        when(lessonService.getLessonsByCourseIdOrdered(1L)).thenReturn(lessons);

        mockMvc.perform(get("/api/lessons/course/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Тестовый урок")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Тестовый урок 2")));
        
        verify(lessonService).getLessonsByCourseIdOrdered(1L);
    }

    @Test
    @DisplayName("Получение уроков с контентом для курса")
    void getContentLessonsByCourseId_ShouldReturnContentLessons() throws Exception {
        when(lessonService.getContentLessonsByCourseId(1L)).thenReturn(List.of(lesson));

        mockMvc.perform(get("/api/lessons/course/1/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Тестовый урок")))
                .andExpect(jsonPath("$[0].content", is("Содержание тестового урока")));
        
        verify(lessonService).getContentLessonsByCourseId(1L);
    }

    @Test
    @DisplayName("Получение урока по ID")
    void getLessonById_ShouldReturnLesson() throws Exception {
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(lesson));

        mockMvc.perform(get("/api/lessons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Тестовый урок")))
                .andExpect(jsonPath("$.content", is("Содержание тестового урока")));
        
        verify(lessonService).getLessonById(1L);
    }

    @Test
    @DisplayName("Получение несуществующего урока")
    void getLessonById_NonExistentLesson_ShouldReturn404() throws Exception {
        when(lessonService.getLessonById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/lessons/99"))
                .andExpect(status().isNotFound());
        
        verify(lessonService).getLessonById(99L);
    }

    @Test
    @DisplayName("Создание нового урока")
    void createLesson_ShouldCreateAndReturnLesson() throws Exception {
        when(courseService.isCourseOwnedByTeacher(1L, 1L)).thenReturn(true);
        when(lessonService.createLesson(any(Lesson.class), eq(1L))).thenReturn(lesson);

        mockMvc.perform(post("/api/lessons/course/1")

                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lesson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Тестовый урок")));
        
        verify(courseService).isCourseOwnedByTeacher(1L, 1L);
        verify(lessonService).createLesson(any(Lesson.class), eq(1L));
    }

    @Test
    @DisplayName("Создание урока в чужом курсе")
    void createLesson_NotOwnedByCourse_ShouldReturn403() throws Exception {
        when(courseService.isCourseOwnedByTeacher(1L, 1L)).thenReturn(false);

        mockMvc.perform(post("/api/lessons/course/1")

                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lesson)))
                .andExpect(status().isForbidden());
        
        verify(courseService).isCourseOwnedByTeacher(1L, 1L);
        verify(lessonService, never()).createLesson(any(Lesson.class), anyLong());
    }

    @Test
    @DisplayName("Обновление урока")
    void updateLesson_ShouldUpdateAndReturnLesson() throws Exception {
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(lesson));
        when(courseService.isCourseOwnedByTeacher(1L, 1L)).thenReturn(true);
        when(lessonService.updateLesson(eq(1L), any(Lesson.class))).thenReturn(lesson);

        mockMvc.perform(put("/api/lessons/1")

                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lesson)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Тестовый урок")));
        
        verify(lessonService).getLessonById(1L);
        verify(courseService).isCourseOwnedByTeacher(1L, 1L);
        verify(lessonService).updateLesson(eq(1L), any(Lesson.class));
    }

    @Test
    @DisplayName("Обновление урока в чужом курсе")
    void updateLesson_NotOwnedByCourse_ShouldReturn403() throws Exception {
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(lesson));
        when(courseService.isCourseOwnedByTeacher(1L, 1L)).thenReturn(false);

        mockMvc.perform(put("/api/lessons/1")

                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lesson)))
                .andExpect(status().isForbidden());
        
        verify(lessonService).getLessonById(1L);
        verify(courseService).isCourseOwnedByTeacher(1L, 1L);
        verify(lessonService, never()).updateLesson(anyLong(), any(Lesson.class));
    }

    @Test
    @DisplayName("Изменение порядка урока")
    void changeLessonOrder_ShouldUpdateAndReturnLesson() throws Exception {
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(lesson));
        when(courseService.isCourseOwnedByTeacher(1L, 1L)).thenReturn(true);
        when(lessonService.changeLessonOrder(eq(1L), eq(3))).thenReturn(lesson);

        mockMvc.perform(patch("/api/lessons/1/order")

                .contentType(MediaType.APPLICATION_JSON)
                .content("3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Тестовый урок")));
        
        verify(lessonService).getLessonById(1L);
        verify(courseService).isCourseOwnedByTeacher(1L, 1L);
        verify(lessonService).changeLessonOrder(eq(1L), eq(3));
    }

    @Test
    @DisplayName("Изменение флага контента урока")
    void changeLessonContentFlag_ShouldUpdateAndReturnLesson() throws Exception {
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(lesson));
        when(courseService.isCourseOwnedByTeacher(1L, 1L)).thenReturn(true);
        when(lessonService.changeLessonContentFlag(eq(1L), eq(false))).thenReturn(lesson);

        mockMvc.perform(patch("/api/lessons/1/content-flag")

                .contentType(MediaType.APPLICATION_JSON)
                .content("false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Тестовый урок")));
        
        verify(lessonService).getLessonById(1L);
        verify(courseService).isCourseOwnedByTeacher(1L, 1L);
        verify(lessonService).changeLessonContentFlag(eq(1L), eq(false));
    }

    @Test
    @DisplayName("Удаление урока")
    void deleteLesson_ShouldDeleteLesson() throws Exception {
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(lesson));
        when(courseService.isCourseOwnedByTeacher(1L, 1L)).thenReturn(true);
        doNothing().when(lessonService).deleteLesson(1L);

        mockMvc.perform(delete("/api/lessons/1")
)
                .andExpect(status().isNoContent());
        
        verify(lessonService).getLessonById(1L);
        verify(courseService).isCourseOwnedByTeacher(1L, 1L);
        verify(lessonService).deleteLesson(1L);
    }

    @Test
    @DisplayName("Удаление урока в чужом курсе")
    void deleteLesson_NotOwnedByCourse_ShouldReturn403() throws Exception {
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(lesson));
        when(courseService.isCourseOwnedByTeacher(1L, 1L)).thenReturn(false);

        mockMvc.perform(delete("/api/lessons/1")
)
                .andExpect(status().isForbidden());
        
        verify(lessonService).getLessonById(1L);
        verify(courseService).isCourseOwnedByTeacher(1L, 1L);
        verify(lessonService, never()).deleteLesson(anyLong());
    }
}
