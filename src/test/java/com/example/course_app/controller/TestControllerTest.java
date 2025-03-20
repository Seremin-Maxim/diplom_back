package com.example.course_app.controller;

import com.example.course_app.entity.Role;
import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.entity.tests.TestType;
import com.example.course_app.service.courses.CourseService;
import com.example.course_app.service.lessons.LessonService;
import com.example.course_app.service.tests.TestService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

// Используем полное имя для класса Test из пакета тестов
import com.example.course_app.entity.tests.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestControllerTest {
    
    // Миксин для игнорирования циклических ссылок при сериализации Test
    @JsonIgnoreProperties({"lesson", "questions"})
    abstract static class TestMixin {}
    

    private MockMvc mockMvc;

    @Mock
    private TestService testService;

    @Mock
    private LessonService lessonService;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private TestController testControllerOriginal;
    
    private TestController testController;
    
    private ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private User teacher;
    private Course course;
    private Lesson lesson;
    private com.example.course_app.entity.tests.Test test1;
    private com.example.course_app.entity.tests.Test test2;
    private List<com.example.course_app.entity.tests.Test> tests;

    @BeforeEach
    public void setUp() {
        // Настраиваем ObjectMapper для работы с Java 8 date/time типами
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        // Создаем тестовые данные
        teacher = new User();
        teacher.setId(1L);
        teacher.setEmail("teacher@example.com");
        teacher.setFirstName("Иван");
        teacher.setLastName("Преподаватель");
        teacher.setRole(Role.TEACHER);

        course = new Course();
        course.setId(1L);
        course.setTitle("Курс программирования");
        course.setDescription("Описание курса");
        course.setTeacher(teacher);

        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("Урок 1");
        lesson.setContent("Содержание урока");
        lesson.setCourse(course);
        lesson.setOrderNumber(1);
        lesson.setIsContent(true);

        test1 = new com.example.course_app.entity.tests.Test();
        test1.setId(1L);
        test1.setTitle("Тест 1");
        test1.setType(TestType.SINGLE_CHOICE);
        test1.setRequiresManualCheck(false);
        test1.setLesson(lesson);
        test1.setCreatedAt(LocalDateTime.now());
        test1.setUpdatedAt(LocalDateTime.now());

        test2 = new com.example.course_app.entity.tests.Test();
        test2.setId(2L);
        test2.setTitle("Тест 2");
        test2.setType(TestType.MULTIPLE_CHOICE);
        test2.setRequiresManualCheck(true);
        test2.setLesson(lesson);
        test2.setCreatedAt(LocalDateTime.now());
        test2.setUpdatedAt(LocalDateTime.now());

        tests = Arrays.asList(test1, test2);
        
        // Создаем шпиона для контроллера, чтобы мочь мокировать метод getUserIdFromAuthentication
        testController = spy(testControllerOriginal);
        
        // Мокируем метод getUserIdFromAuthentication, чтобы он возвращал ID пользователя
        doReturn(1L).when(testController).getUserIdFromAuthentication(any());
        
        // Добавляем миксин для игнорирования циклических ссылок
        objectMapper.addMixIn(Test.class, TestMixin.class);
        
        // Настраиваем MockMvc
        mockMvc = MockMvcBuilders
                .standaloneSetup(testController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
        
        // Настраиваем моки для сервисов
        lenient().when(lessonService.getLessonById(1L)).thenReturn(Optional.of(lesson));
        lenient().when(courseService.isCourseOwnedByTeacher(1L, 1L)).thenReturn(true);
    }

    @org.junit.jupiter.api.Test
    public void getTestById_ShouldReturnTest() throws Exception {
        when(testService.getTestById(1L)).thenReturn(Optional.of(test1));

        mockMvc.perform(get("/api/tests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Тест 1")))
                .andExpect(jsonPath("$.type", is("SINGLE_CHOICE")));

        verify(testService).getTestById(1L);
    }

    @org.junit.jupiter.api.Test
    public void getTestById_NonExistentTest_ShouldReturn404() throws Exception {
        when(testService.getTestById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tests/99"))
                .andExpect(status().isNotFound());

        verify(testService).getTestById(99L);
    }

    @org.junit.jupiter.api.Test
    public void getTestsByLessonId_ShouldReturnAllTests() throws Exception {
        when(testService.getTestsByLessonId(1L)).thenReturn(tests);

        mockMvc.perform(get("/api/tests/lesson/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Тест 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Тест 2")));

        verify(testService).getTestsByLessonId(1L);
    }

    @org.junit.jupiter.api.Test
    public void getTestsByLessonIdAndType_ShouldReturnFilteredTests() throws Exception {
        when(testService.getTestsByLessonIdAndType(1L, TestType.SINGLE_CHOICE))
                .thenReturn(Arrays.asList(test1));

        mockMvc.perform(get("/api/tests/lesson/1/type/SINGLE_CHOICE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Тест 1")));

        verify(testService).getTestsByLessonIdAndType(1L, TestType.SINGLE_CHOICE);
    }

    @org.junit.jupiter.api.Test
    public void createTest_ShouldCreateAndReturnTest() throws Exception {
        when(testService.createTest(eq(1L), anyString(), any(TestType.class), anyBoolean()))
                .thenReturn(test1);

        mockMvc.perform(post("/api/tests/lesson/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(test1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Тест 1")));

        verify(testService).createTest(eq(1L), anyString(), any(TestType.class), anyBoolean());
    }

    @org.junit.jupiter.api.Test
    public void createTest_NotAuthorized_ShouldReturn403() throws Exception {
        when(courseService.isCourseOwnedByTeacher(1L, 1L)).thenReturn(false);

        mockMvc.perform(post("/api/tests/lesson/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(test1)))
                .andExpect(status().isForbidden());

        verify(courseService).isCourseOwnedByTeacher(1L, 1L);
        verify(testService, never()).createTest(anyLong(), anyString(), any(TestType.class), anyBoolean());
    }

    @org.junit.jupiter.api.Test
    public void updateTest_ShouldUpdateAndReturnTest() throws Exception {
        when(testService.getTestById(1L)).thenReturn(Optional.of(test1));
        when(testService.updateTest(eq(1L), anyString(), any(TestType.class), anyBoolean()))
                .thenReturn(test1);

        mockMvc.perform(put("/api/tests/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(test1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Тест 1")));

        verify(testService).updateTest(eq(1L), anyString(), any(TestType.class), anyBoolean());
    }

    @org.junit.jupiter.api.Test
    public void updateTest_NotAuthorized_ShouldReturn403() throws Exception {
        when(testService.getTestById(1L)).thenReturn(Optional.of(test1));
        when(courseService.isCourseOwnedByTeacher(1L, 1L)).thenReturn(false);

        mockMvc.perform(put("/api/tests/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(test1)))
                .andExpect(status().isForbidden());

        verify(courseService).isCourseOwnedByTeacher(1L, 1L);
        verify(testService).getTestById(1L);
        verify(testService, never()).updateTest(anyLong(), anyString(), any(TestType.class), anyBoolean());
    }

    @org.junit.jupiter.api.Test
    public void deleteTest_ShouldDeleteAndReturn204() throws Exception {
        when(testService.getTestById(1L)).thenReturn(Optional.of(test1));
        doNothing().when(testService).deleteTest(1L);

        mockMvc.perform(delete("/api/tests/1"))
                .andExpect(status().isNoContent());

        verify(testService).deleteTest(1L);
    }

    @org.junit.jupiter.api.Test
    public void deleteTest_NotAuthorized_ShouldReturn403() throws Exception {
        when(testService.getTestById(1L)).thenReturn(Optional.of(test1));
        when(courseService.isCourseOwnedByTeacher(1L, 1L)).thenReturn(false);

        mockMvc.perform(delete("/api/tests/1"))
                .andExpect(status().isForbidden());

        verify(courseService).isCourseOwnedByTeacher(1L, 1L);
        // Тестовый сервис вызывается для получения теста по ID, но не для удаления
        verify(testService).getTestById(1L);
        verify(testService, never()).deleteTest(anyLong());
    }

    @org.junit.jupiter.api.Test
    public void getQuestionCount_ShouldReturnCount() throws Exception {
        when(testService.getTestById(1L)).thenReturn(Optional.of(test1));
        when(testService.getQuestionCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/tests/1/question-count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(testService).getQuestionCount(1L);
    }

    @org.junit.jupiter.api.Test
    public void getMaxPoints_ShouldReturnMaxPoints() throws Exception {
        when(testService.getTestById(1L)).thenReturn(Optional.of(test1));
        when(testService.getMaxPoints(1L)).thenReturn(10);

        mockMvc.perform(get("/api/tests/1/max-points"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));

        verify(testService).getMaxPoints(1L);
    }
}
