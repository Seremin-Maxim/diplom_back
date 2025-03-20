package com.example.course_app.controller;

import com.example.course_app.entity.Role;
import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.lessons.Lesson;
// Используем полное имя класса для избежания конфликта с org.junit.jupiter.api.Test
import com.example.course_app.entity.tests.TestType;
import com.example.course_app.repository.UserRepository;
import com.example.course_app.repository.CourseRepository;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.repository.TestRepository;
import com.example.course_app.config.JwtTokenProvider;
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
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;



//import org.springframework.web.context.WebApplicationContext;



import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TestControllerIT {

    @Autowired
    private MockMvc mockMvc;
    
    //@Autowired
    //private WebApplicationContext webApplicationContext;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            
    @Autowired
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

    // Миксины для игнорирования циклических ссылок при сериализации
    @JsonIgnoreProperties({"lesson", "questions", "hibernateLazyInitializer", "handler"})
    abstract static class TestEntityMixin {}
    
    @JsonIgnoreProperties({"course", "tests", "hibernateLazyInitializer", "handler"})
    abstract static class LessonMixin {}
    
    @JsonIgnoreProperties({"teacher", "lessons", "students", "hibernateLazyInitializer", "handler"})
    abstract static class CourseMixin {}
    
    @JsonIgnoreProperties({"courses", "submissions", "hibernateLazyInitializer", "handler"})
    abstract static class UserMixin {}

    private User teacher;
    private Course course;
    private Lesson lesson;
    private com.example.course_app.entity.tests.Test test1;
    private com.example.course_app.entity.tests.Test test2;
    private String teacherToken;

    @BeforeEach
    public void setUp() {
        // Настраиваем ObjectMapper для игнорирования циклических ссылок
        objectMapper.addMixIn(com.example.course_app.entity.tests.Test.class, TestEntityMixin.class);
        objectMapper.addMixIn(com.example.course_app.entity.lessons.Lesson.class, LessonMixin.class);
        objectMapper.addMixIn(com.example.course_app.entity.courses.Course.class, CourseMixin.class);
        objectMapper.addMixIn(com.example.course_app.entity.User.class, UserMixin.class);
        
        // Добавляем поддержку Hibernate-прокси
        SimpleModule hibernateModule = new SimpleModule();
        hibernateModule.addSerializer(HibernateProxy.class, new ToStringSerializer());
        objectMapper.registerModule(hibernateModule);
        
        // Устанавливаем наш ObjectMapper в конвертер
        mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);
        
        // Настраиваем ObjectMapper для использования в тестах
        // В интеграционных тестах MockMvc инициализируется автоматически с помощью @Autowired
        // Очищаем базу данных перед каждым тестом
        testRepository.deleteAll();
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

        lesson = new Lesson();
        lesson.setTitle("Урок 1");
        lesson.setContent("Содержание урока");
        lesson.setCourse(course);
        lesson.setOrderNumber(1);
        lesson.setIsContent(true);
        lesson = lessonRepository.save(lesson);

        test1 = new com.example.course_app.entity.tests.Test();
        test1.setTitle("Тест 1");
        test1.setType(TestType.SINGLE_CHOICE);
        test1.setRequiresManualCheck(false);
        test1.setLesson(lesson);
        test1.setCreatedAt(LocalDateTime.now());
        test1.setUpdatedAt(LocalDateTime.now());
        test1 = testRepository.save(test1);

        test2 = new com.example.course_app.entity.tests.Test();
        test2.setTitle("Тест 2");
        test2.setType(TestType.MULTIPLE_CHOICE);
        test2.setRequiresManualCheck(true);
        test2.setLesson(lesson);
        test2.setCreatedAt(LocalDateTime.now());
        test2.setUpdatedAt(LocalDateTime.now());
        test2 = testRepository.save(test2);
    }

    @Test
    public void getTestById_ShouldReturnTestDTO() throws Exception {
        mockMvc.perform(get("/api/tests/" + test1.getId())
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(test1.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Тест 1")))
                .andExpect(jsonPath("$.type", is("SINGLE_CHOICE")))
                .andExpect(jsonPath("$.lessonId", is(lesson.getId().intValue())));
    }

    @Test
    public void getTestById_NonExistentTest_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/tests/999")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTestsByLessonId_ShouldReturnAllTestDTOs() throws Exception {
        mockMvc.perform(get("/api/tests/lesson/" + lesson.getId())
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(test1.getId().intValue())))
                .andExpect(jsonPath("$[0].title", is("Тест 1")))
                .andExpect(jsonPath("$[0].lessonId", is(lesson.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(test2.getId().intValue())))
                .andExpect(jsonPath("$[1].title", is("Тест 2")))
                .andExpect(jsonPath("$[1].lessonId", is(lesson.getId().intValue())));
    }

    @Test
    public void getTestsByLessonIdAndType_ShouldReturnFilteredTestDTOs() throws Exception {
        mockMvc.perform(get("/api/tests/lesson/" + lesson.getId() + "/type/SINGLE_CHOICE")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(test1.getId().intValue())))
                .andExpect(jsonPath("$[0].title", is("Тест 1")))
                .andExpect(jsonPath("$[0].lessonId", is(lesson.getId().intValue())));
    }

    @Test
    public void createTest_ShouldCreateAndReturnTestDTO() throws Exception {
        com.example.course_app.entity.tests.Test newTest = new com.example.course_app.entity.tests.Test();
        newTest.setTitle("Новый тест");
        newTest.setType(TestType.ESSAY);
        newTest.setRequiresManualCheck(true);

        mockMvc.perform(post("/api/tests/lesson/" + lesson.getId())
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Новый тест")))
                .andExpect(jsonPath("$.type", is("ESSAY")))
                .andExpect(jsonPath("$.requiresManualCheck", is(true)))
                .andExpect(jsonPath("$.lessonId", is(lesson.getId().intValue())));

        // Проверяем, что тест действительно создан в базе данных
        List<com.example.course_app.entity.tests.Test> tests = testRepository.findByLessonId(lesson.getId());
        assert tests.size() == 3;
    }

    @Test
    public void updateTest_ShouldUpdateAndReturnTestDTO() throws Exception {
        test1.setTitle("Обновленный тест");
        test1.setType(TestType.ESSAY);
        test1.setRequiresManualCheck(true);

        mockMvc.perform(put("/api/tests/" + test1.getId())
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(test1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(test1.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Обновленный тест")))
                .andExpect(jsonPath("$.type", is("ESSAY")))
                .andExpect(jsonPath("$.requiresManualCheck", is(true)))
                .andExpect(jsonPath("$.lessonId", is(lesson.getId().intValue())));

        // Проверяем, что тест действительно обновлен в базе данных
        com.example.course_app.entity.tests.Test updatedTest = testRepository.findById(test1.getId()).orElseThrow();
        assert updatedTest.getTitle().equals("Обновленный тест");
        assert updatedTest.getType() == TestType.ESSAY;
        assert updatedTest.isRequiresManualCheck();
    }

    @Test
    public void deleteTest_ShouldDeleteAndReturn204() throws Exception {
        mockMvc.perform(delete("/api/tests/" + test1.getId())
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isNoContent());

        // Проверяем, что тест действительно удален из базы данных
        assert testRepository.findById(test1.getId()).isEmpty();
    }

    @Test
    public void getQuestionCount_ShouldReturnCount() throws Exception {
        // В данном случае у теста нет вопросов, поэтому ожидаем 0
        mockMvc.perform(get("/api/tests/" + test1.getId() + "/question-count")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    public void getMaxPoints_ShouldReturnMaxPoints() throws Exception {
        // В данном случае у теста нет вопросов, поэтому ожидаем 0
        mockMvc.perform(get("/api/tests/" + test1.getId() + "/max-points")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }
}
