package com.example.course_app.controller;

import com.example.course_app.entity.Role;
import com.example.course_app.entity.User;
import com.example.course_app.entity.answers.Answer;
import com.example.course_app.entity.questions.Question;
import com.example.course_app.entity.questions.QuestionType;
import com.example.course_app.repository.*;
import com.example.course_app.config.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для AnswerController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AnswerControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User teacher;
    private User student;
    private com.example.course_app.entity.tests.Test test;
    private Question question;
    private Answer answer;
    private String teacherToken;
    private String studentToken;

    @BeforeEach
    public void setup() {
        // Создаем пользователей
        teacher = new User();
        teacher.setEmail("teacher@example.com");
        teacher.setPassword(passwordEncoder.encode("password"));
        teacher.setFirstName("Teacher");
        teacher.setLastName("Test");
        teacher.setRole(Role.TEACHER);
        teacher = userRepository.save(teacher);

        student = new User();
        student.setEmail("student@example.com");
        student.setPassword(passwordEncoder.encode("password"));
        student.setFirstName("Student");
        student.setLastName("Test");
        student.setRole(Role.USER);
        student = userRepository.save(student);

        // Создаем урок для теста
        com.example.course_app.entity.lessons.Lesson lesson = new com.example.course_app.entity.lessons.Lesson();
        lesson.setTitle("Test Lesson");
        lesson.setContent("Test Content");
        // Здесь нужно создать курс и связать с уроком
        com.example.course_app.entity.courses.Course course = new com.example.course_app.entity.courses.Course();
        course.setTitle("Test Course");
        course.setTeacher(teacher);
        course = courseRepository.save(course);
        lesson.setCourse(course);
        lesson = lessonRepository.save(lesson);
        
        // Создаем тест
        test = new com.example.course_app.entity.tests.Test();
        test.setTitle("Test Title");
        test.setType(com.example.course_app.entity.tests.TestType.SINGLE_CHOICE);
        test.setRequiresManualCheck(false);
        test.setLesson(lesson);
        test = testRepository.save(test);

        // Создаем вопрос
        question = new Question();
        question.setTest(test);
        question.setText("Question Text");
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setPoints(10);
        question = questionRepository.save(question);

        // Создаем ответ
        answer = new Answer();
        answer.setQuestion(question);
        answer.setText("Answer Text");
        answer.setCorrect(true);
        answer = answerRepository.save(answer);

        // Генерируем токены
        Authentication teacherAuth = new UsernamePasswordAuthenticationToken(
                teacher,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
        teacherToken = jwtTokenProvider.generateToken(teacherAuth);

        Authentication studentAuth = new UsernamePasswordAuthenticationToken(
                student,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        studentToken = jwtTokenProvider.generateToken(studentAuth);
    }

    // Тест получения ответа по ID
    @Test
    public void testGetAnswerById() throws Exception {
        mockMvc.perform(get("/api/answers/{id}", answer.getId())
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(answer.getId()))
                .andExpect(jsonPath("$.text").value(answer.getText()))
                .andExpect(jsonPath("$.correct").value(answer.isCorrect()));
    }

    // Тест получения ответа по несуществующему ID
    @Test
    public void testGetAnswerByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/answers/{id}", 999L)
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // Тест получения ответов для вопроса
    @Test
    public void testGetAnswersByQuestionId() throws Exception {
        mockMvc.perform(get("/api/answers/question/{questionId}", question.getId())
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").value(answer.getId()))
                .andExpect(jsonPath("$[0].text").value(answer.getText()));
    }

    // Тест создания ответа
    @Test
    public void testCreateAnswer() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", "New Answer");
        requestBody.put("isCorrect", false);

        mockMvc.perform(post("/api/answers/question/{questionId}", question.getId())
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("New Answer"))
                .andExpect(jsonPath("$.correct").value(false));
    }

    // Тест создания ответа студентом (должен быть запрещен)
    @Test
    public void testCreateAnswerByStudent() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", "Student Answer");
        requestBody.put("isCorrect", false);

        mockMvc.perform(post("/api/answers/question/{questionId}", question.getId())
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    // Тест обновления ответа
    @Test
    public void testUpdateAnswer() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", "Updated Answer");
        requestBody.put("isCorrect", false);

        mockMvc.perform(put("/api/answers/{id}", answer.getId())
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated Answer"))
                .andExpect(jsonPath("$.correct").value(false));
    }

    // Тест обновления ответа студентом (должен быть запрещен)
    @Test
    public void testUpdateAnswerByStudent() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", "Student Update");
        requestBody.put("isCorrect", false);

        mockMvc.perform(put("/api/answers/{id}", answer.getId())
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    // Тест удаления ответа
    @Test
    public void testDeleteAnswer() throws Exception {
        mockMvc.perform(delete("/api/answers/{id}", answer.getId())
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        // Проверяем, что ответ действительно удален
        mockMvc.perform(get("/api/answers/{id}", answer.getId())
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isNotFound());
    }

    // Тест удаления ответа студентом (должен быть запрещен)
    @Test
    public void testDeleteAnswerByStudent() throws Exception {
        mockMvc.perform(delete("/api/answers/{id}", answer.getId())
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    // Тест создания набора ответов для вопроса с одним правильным ответом
    @Test
    public void testCreateSingleChoiceAnswers() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("correctAnswerText", "Correct Answer");
        requestBody.put("incorrectAnswersText", Arrays.asList("Wrong 1", "Wrong 2", "Wrong 3"));

        mockMvc.perform(post("/api/answers/question/{questionId}/single-choice", question.getId())
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].text").value("Correct Answer"))
                .andExpect(jsonPath("$[0].correct").value(true));
    }

    // Тест создания набора ответов для вопроса с несколькими правильными ответами
    @Test
    public void testCreateMultipleChoiceAnswers() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("correctAnswersText", Arrays.asList("Correct 1", "Correct 2"));
        requestBody.put("incorrectAnswersText", Arrays.asList("Wrong 1", "Wrong 2"));

        mockMvc.perform(post("/api/answers/question/{questionId}/multiple-choice", question.getId())
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].text").value("Correct 1"))
                .andExpect(jsonPath("$[0].correct").value(true))
                .andExpect(jsonPath("$[1].text").value("Correct 2"))
                .andExpect(jsonPath("$[1].correct").value(true));
    }

    // Тест проверки принадлежности ответа вопросу
    @Test
    public void testIsAnswerBelongsToQuestion() throws Exception {
        mockMvc.perform(get("/api/answers/{answerId}/belongs-to-question/{questionId}", 
                answer.getId(), question.getId())
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.belongs").value(true));
    }

    // Тест проверки, является ли ответ правильным
    @Test
    public void testIsAnswerCorrect() throws Exception {
        mockMvc.perform(get("/api/answers/{id}/is-correct", answer.getId())
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(true));
    }
}
