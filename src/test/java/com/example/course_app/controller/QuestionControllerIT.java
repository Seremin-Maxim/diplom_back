package com.example.course_app.controller;

import com.example.course_app.entity.User;
import com.example.course_app.entity.Role;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.entity.questions.Question;
import com.example.course_app.entity.questions.QuestionType;
import com.example.course_app.entity.tests.Test;
import com.example.course_app.entity.tests.TestType;
import com.example.course_app.repository.UserRepository;
import com.example.course_app.repository.CourseRepository;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.repository.TestRepository;
import com.example.course_app.repository.QuestionRepository;
import com.example.course_app.config.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
//import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

//import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class QuestionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestionRepository questionRepository;

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

    private ObjectMapper objectMapper;

    private User teacher;
    private User student;
    private Course course;
    private Lesson lesson;
    private Test test;
    private Question question;

    @BeforeEach
    void setUp() {
        // Настраиваем ObjectMapper для корректной сериализации/десериализации
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Очищаем репозитории перед каждым тестом
        questionRepository.deleteAll();
        testRepository.deleteAll();
        lessonRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем тестовых пользователей
        teacher = new User();
        teacher.setEmail("teacher@example.com");
        teacher.setPassword("password");
        teacher.setRole(Role.TEACHER);
        teacher.setFirstName("Иван");
        teacher.setLastName("Преподавателев");
        userRepository.save(teacher);

        student = new User();
        student.setEmail("student@example.com");
        student.setPassword("password");
        student.setRole(Role.USER);
        student.setFirstName("Петр");
        student.setLastName("Студентов");
        userRepository.save(student);

        // Создаем тестовый курс
        course = new Course();
        course.setTitle("Тестовый курс");
        course.setDescription("Описание тестового курса");
        course.setTeacher(teacher);
        courseRepository.save(course);

        // Создаем тестовый урок
        lesson = new Lesson();
        lesson.setTitle("Тестовый урок");
        lesson.setContent("Содержание тестового урока");
        lesson.setCourse(course);
        lessonRepository.save(lesson);

        // Создаем тестовый тест
        test = new Test();
        test.setTitle("Тестовый тест");
        test.setType(TestType.SINGLE_CHOICE);
        test.setRequiresManualCheck(false);
        test.setLesson(lesson);
        testRepository.save(test);

        // Создаем тестовый вопрос
        question = new Question();
        question.setText("Тестовый вопрос");
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setPoints(2);
        question.setTest(test);
        questionRepository.save(question);
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Получение вопроса по ID")
    void getQuestionById_ShouldReturnQuestion() throws Exception {
        mockMvc.perform(get("/api/questions/{id}", question.getId())
                .header("Authorization", "Bearer " + jwtTokenProvider.createToken(teacher.getEmail(), "TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(question.getId()))
                .andExpect(jsonPath("$.text").value(question.getText()))
                .andExpect(jsonPath("$.type").value(question.getType().toString()))
                .andExpect(jsonPath("$.points").value(question.getPoints()))
                .andExpect(jsonPath("$.testId").value(test.getId()));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Получение несуществующего вопроса")
    void getQuestionById_WithNonExistingId_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/questions/999")
                .header("Authorization", "Bearer " + jwtTokenProvider.createToken(teacher.getEmail(), "TEACHER")))
                .andExpect(status().isNotFound());
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Получение вопросов по ID теста")
    void getQuestionsByTestId_ShouldReturnQuestions() throws Exception {
        mockMvc.perform(get("/api/questions/test/{testId}", test.getId())
                .header("Authorization", "Bearer " + jwtTokenProvider.createToken(teacher.getEmail(), "TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(question.getId()))
                .andExpect(jsonPath("$[0].text").value(question.getText()))
                .andExpect(jsonPath("$[0].type").value(question.getType().toString()))
                .andExpect(jsonPath("$[0].points").value(question.getPoints()))
                .andExpect(jsonPath("$[0].testId").value(test.getId()));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Создание нового вопроса")
    void createQuestion_ShouldCreateAndReturnQuestion() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", "Новый вопрос");
        requestBody.put("type", "MULTIPLE_CHOICE");
        requestBody.put("points", 3);

        mockMvc.perform(post("/api/questions/test/{testId}", test.getId())
                .header("Authorization", "Bearer " + jwtTokenProvider.createToken(teacher.getEmail(), "TEACHER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Новый вопрос"))
                .andExpect(jsonPath("$.type").value("MULTIPLE_CHOICE"))
                .andExpect(jsonPath("$.points").value(3))
                .andExpect(jsonPath("$.testId").value(test.getId()));

        // Проверяем, что вопрос действительно создан в базе данных
        List<Question> questions = questionRepository.findByTestId(test.getId());
        org.junit.jupiter.api.Assertions.assertEquals(2, questions.size());
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Создание вопроса студентом (должно быть запрещено)")
    void createQuestion_ByStudent_ShouldReturn403() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", "Новый вопрос");
        requestBody.put("type", "MULTIPLE_CHOICE");
        requestBody.put("points", 3);

        mockMvc.perform(post("/api/questions/test/{testId}", test.getId())
                .header("Authorization", "Bearer " + jwtTokenProvider.createToken(student.getEmail(), "USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden());

        // Проверяем, что вопрос не был создан
        List<Question> questions = questionRepository.findByTestId(test.getId());
        org.junit.jupiter.api.Assertions.assertEquals(1, questions.size());
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Обновление вопроса")
    void updateQuestion_ShouldUpdateAndReturnQuestion() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", "Обновленный вопрос");
        requestBody.put("type", "TEXT_INPUT");
        requestBody.put("points", 5);

        mockMvc.perform(put("/api/questions/{id}", question.getId())
                .header("Authorization", "Bearer " + jwtTokenProvider.createToken(teacher.getEmail(), "TEACHER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(question.getId()))
                .andExpect(jsonPath("$.text").value("Обновленный вопрос"))
                .andExpect(jsonPath("$.type").value("TEXT_INPUT"))
                .andExpect(jsonPath("$.points").value(5))
                .andExpect(jsonPath("$.testId").value(test.getId()));

        // Проверяем, что вопрос действительно обновлен в базе данных
        Question updatedQuestion = questionRepository.findById(question.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("Обновленный вопрос", updatedQuestion.getText());
        org.junit.jupiter.api.Assertions.assertEquals(QuestionType.TEXT_INPUT, updatedQuestion.getType());
        org.junit.jupiter.api.Assertions.assertEquals(5, updatedQuestion.getPoints());
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Обновление вопроса студентом (должно быть запрещено)")
    void updateQuestion_ByStudent_ShouldReturn403() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", "Обновленный вопрос");
        requestBody.put("type", "TEXT_INPUT");
        requestBody.put("points", 5);

        mockMvc.perform(put("/api/questions/{id}", question.getId())
                .header("Authorization", "Bearer " + jwtTokenProvider.createToken(student.getEmail(), "USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden());

        // Проверяем, что вопрос не был обновлен
        Question unchangedQuestion = questionRepository.findById(question.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("Тестовый вопрос", unchangedQuestion.getText());
        org.junit.jupiter.api.Assertions.assertEquals(QuestionType.SINGLE_CHOICE, unchangedQuestion.getType());
        org.junit.jupiter.api.Assertions.assertEquals(2, unchangedQuestion.getPoints());
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Удаление вопроса")
    void deleteQuestion_ShouldDeleteQuestion() throws Exception {
        mockMvc.perform(delete("/api/questions/{id}", question.getId())
                .header("Authorization", "Bearer " + jwtTokenProvider.createToken(teacher.getEmail(), "TEACHER")))
                .andExpect(status().isNoContent());

        // Проверяем, что вопрос действительно удален из базы данных
        org.junit.jupiter.api.Assertions.assertFalse(questionRepository.existsById(question.getId()));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Удаление вопроса студентом (должно быть запрещено)")
    void deleteQuestion_ByStudent_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/questions/{id}", question.getId())
                .header("Authorization", "Bearer " + jwtTokenProvider.createToken(student.getEmail(), "USER")))
                .andExpect(status().isForbidden());

        // Проверяем, что вопрос не был удален
        org.junit.jupiter.api.Assertions.assertTrue(questionRepository.existsById(question.getId()));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Проверка принадлежности вопроса тесту")
    void isQuestionBelongsToTest_WhenBelongs_ShouldReturnTrue() throws Exception {
        mockMvc.perform(get("/api/questions/{questionId}/belongs-to-test/{testId}", question.getId(), test.getId())
                .header("Authorization", "Bearer " + jwtTokenProvider.createToken(teacher.getEmail(), "TEACHER")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Проверка принадлежности вопроса другому тесту")
    void isQuestionBelongsToTest_WhenDoesNotBelong_ShouldReturnFalse() throws Exception {
        // Создаем другой тест
        Test anotherTest = new Test();
        anotherTest.setTitle("Другой тест");
        anotherTest.setType(TestType.ESSAY);
        anotherTest.setRequiresManualCheck(true);
        anotherTest.setLesson(lesson);
        testRepository.save(anotherTest);

        mockMvc.perform(get("/api/questions/{questionId}/belongs-to-test/{testId}", question.getId(), anotherTest.getId())
                .header("Authorization", "Bearer " + jwtTokenProvider.createToken(teacher.getEmail(), "TEACHER")))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
