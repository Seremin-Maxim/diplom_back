package com.example.course_app.controller;

import com.example.course_app.entity.Role;
import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.repository.UserRepository;
import com.example.course_app.repository.CourseRepository;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.config.JwtTokenProvider;
import com.example.course_app.config.TestEnvironmentInitializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.hibernate.proxy.HibernateProxy;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
public class LessonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    // Миксины для игнорирования циклических ссылок при сериализации
    @JsonIgnoreProperties({"course", "tests", "hibernateLazyInitializer", "handler"})
    abstract static class LessonMixin {}
    
    @JsonIgnoreProperties({"teacher", "lessons", "students", "hibernateLazyInitializer", "handler"})
    abstract static class CourseMixin {}
    
    @JsonIgnoreProperties({"courses", "submissions", "hibernateLazyInitializer", "handler"})
    abstract static class UserMixin {}

    private User teacher;
    private Course course;
    private Lesson lesson1;
    private Lesson lesson2;
    private String teacherToken;

    @BeforeEach
    public void setUp() {
        // Настраиваем ObjectMapper для игнорирования циклических ссылок
        objectMapper.addMixIn(Lesson.class, LessonMixin.class);
        objectMapper.addMixIn(Course.class, CourseMixin.class);
        objectMapper.addMixIn(User.class, UserMixin.class);
        
        // Добавляем поддержку Hibernate-прокси
        SimpleModule hibernateModule = new SimpleModule();
        hibernateModule.addSerializer(HibernateProxy.class, new ToStringSerializer());
        objectMapper.registerModule(hibernateModule);
        
        // Устанавливаем наш ObjectMapper в конвертер
        mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);
        
        // Очищаем базу данных перед каждым тестом
        lessonRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем тестовые данные
        teacher = new User();
        teacher.setEmail("teacher@example.com");
        teacher.setPassword("password");
        teacher.setFirstName("Иван");
        teacher.setLastName("Преподаватель");
        teacher.setRole(Role.TEACHER);
        teacher = userRepository.save(teacher);

        // Создаем JWT токен для преподавателя
        teacherToken = jwtTokenProvider.createToken(teacher.getEmail(), teacher.getRole().name());

        course = new Course();
        course.setTitle("Курс программирования");
        course.setDescription("Описание курса");
        course.setTeacher(teacher);
        course = courseRepository.save(course);

        lesson1 = new Lesson();
        lesson1.setTitle("Урок 1");
        lesson1.setContent("Содержание урока 1");
        lesson1.setCourse(course);
        lesson1.setOrderNumber(1);
        lesson1.setIsContent(true);
        lesson1 = lessonRepository.save(lesson1);

        lesson2 = new Lesson();
        lesson2.setTitle("Урок 2");
        lesson2.setContent("Содержание урока 2");
        lesson2.setCourse(course);
        lesson2.setOrderNumber(2);
        lesson2.setIsContent(false);
        lesson2 = lessonRepository.save(lesson2);
    }

    @Test
    public void getLessonsByCourseId_ShouldReturnAllLessons() throws Exception {
        mockMvc.perform(get("/api/lessons/course/" + course.getId())
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(lesson1.getId().intValue())))
                .andExpect(jsonPath("$[0].title", is("Урок 1")))
                .andExpect(jsonPath("$[1].id", is(lesson2.getId().intValue())))
                .andExpect(jsonPath("$[1].title", is("Урок 2")));
    }

    @Test
    public void getContentLessonsByCourseId_ShouldReturnContentLessons() throws Exception {
        mockMvc.perform(get("/api/lessons/course/" + course.getId() + "/content")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(lesson1.getId().intValue())))
                .andExpect(jsonPath("$[0].title", is("Урок 1")))
                .andExpect(jsonPath("$[0].content", is("Содержание урока 1")));
    }

    @Test
    public void getLessonById_ShouldReturnLesson() throws Exception {
        mockMvc.perform(get("/api/lessons/" + lesson1.getId())
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(lesson1.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Урок 1")))
                .andExpect(jsonPath("$.content", is("Содержание урока 1")));
    }

    @Test
    public void getLessonById_NonExistentLesson_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/lessons/999")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createLesson_ShouldCreateAndReturnLesson() throws Exception {
        Lesson newLesson = new Lesson();
        newLesson.setTitle("Новый урок");
        newLesson.setContent("Содержание нового урока");
        newLesson.setOrderNumber(3);
        newLesson.setIsContent(true);

        mockMvc.perform(post("/api/lessons/course/" + course.getId())
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLesson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Новый урок")))
                .andExpect(jsonPath("$.content", is("Содержание нового урока")))
                .andExpect(jsonPath("$.orderNumber", is(3)))
                .andExpect(jsonPath("$.isContent", is(true)));

        // Проверяем, что урок действительно создан в базе данных
        List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
        assert lessons.size() == 3;
    }

    @Test
    public void updateLesson_ShouldUpdateAndReturnLesson() throws Exception {
        lesson1.setTitle("Обновленный урок");
        lesson1.setContent("Обновленное содержание урока");
        // Не меняем isContent, так как метод updateLesson не обновляет это поле
    
        mockMvc.perform(put("/api/lessons/" + lesson1.getId())
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lesson1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(lesson1.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Обновленный урок")))
                .andExpect(jsonPath("$.content", is("Обновленное содержание урока")))
                // Ожидаем, что isContent останется true, как было изначально
                .andExpect(jsonPath("$.isContent", is(true)));
    
        // Проверяем, что урок действительно обновлен в базе данных
        Lesson updatedLesson = lessonRepository.findById(lesson1.getId()).orElseThrow();
        assert updatedLesson.getTitle().equals("Обновленный урок");
        assert updatedLesson.getContent().equals("Обновленное содержание урока");
        // isContent должно остаться true
        assert updatedLesson.getIsContent();
    }

    @Test
    public void changeLessonOrder_ShouldUpdateAndReturnLesson() throws Exception {
        mockMvc.perform(patch("/api/lessons/" + lesson1.getId() + "/order")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(lesson1.getId().intValue())))
                .andExpect(jsonPath("$.orderNumber", is(3)));

        // Проверяем, что порядок урока действительно обновлен в базе данных
        Lesson updatedLesson = lessonRepository.findById(lesson1.getId()).orElseThrow();
        assert updatedLesson.getOrderNumber() == 3;
    }

    @Test
    public void changeLessonContentFlag_ShouldUpdateAndReturnLesson() throws Exception {
        mockMvc.perform(patch("/api/lessons/" + lesson1.getId() + "/content-flag")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(lesson1.getId().intValue())))
                .andExpect(jsonPath("$.isContent", is(false)));

        // Проверяем, что флаг контента урока действительно обновлен в базе данных
        Lesson updatedLesson = lessonRepository.findById(lesson1.getId()).orElseThrow();
        assert !updatedLesson.getIsContent();
    }

    @Test
    public void deleteLesson_ShouldDeleteAndReturn204() throws Exception {
        mockMvc.perform(delete("/api/lessons/" + lesson1.getId())
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isNoContent());

        // Проверяем, что урок действительно удален из базы данных
        assert lessonRepository.findById(lesson1.getId()).isEmpty();
    }
}