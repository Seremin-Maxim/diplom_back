package com.example.course_app.controller;

import com.example.course_app.config.JwtTokenProvider;
import com.example.course_app.config.TestEnvironmentInitializer;
import com.example.course_app.dto.CreateStudentAnswerRequest;
import com.example.course_app.dto.GradeStudentAnswerRequest;
import com.example.course_app.dto.UpdateStudentAnswerRequest;
import com.example.course_app.entity.User;
import com.example.course_app.entity.questions.Question;
import com.example.course_app.entity.Role;
import com.example.course_app.entity.questions.QuestionType;
import com.example.course_app.entity.submissions.StudentAnswer;
import com.example.course_app.entity.submissions.StudentSubmission;
import com.example.course_app.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для контроллера ответов студентов.
 * Проверяет взаимодействие контроллера с реальными сервисами и базой данных.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
public class StudentAnswerControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private StudentSubmissionRepository submissionRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private CourseRepository courseRepository;

    private User student;
    private User teacher;
    private com.example.course_app.entity.tests.Test testEntity;
    private Question question;
    private StudentSubmission submission;
    private StudentAnswer studentAnswer;
    private String studentToken;
    private String teacherToken;

    /**
     * Настройка тестовых данных перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        // Создаем пользователя (студента)
        student = new User();
        student.setEmail("student@test.com");
        student.setPassword(passwordEncoder.encode("password"));
        student.setFirstName("Test");
        student.setLastName("Student");
        student.setRole(Role.USER);
        student.setEnabled(true);
        userRepository.save(student);
        
        // Создаем пользователя (преподавателя)
        teacher = new User();
        teacher.setEmail("teacher@test.com");
        teacher.setPassword(passwordEncoder.encode("password"));
        teacher.setFirstName("Test");
        teacher.setLastName("Teacher");
        teacher.setRole(Role.TEACHER);
        teacher.setEnabled(true);
        userRepository.save(teacher);
        
        // Создаем курс
        com.example.course_app.entity.courses.Course course = new com.example.course_app.entity.courses.Course();
        course.setTitle("Тестовый курс");
        course.setTeacher(teacher);
        course = courseRepository.save(course);
        
        // Создаем урок
        com.example.course_app.entity.lessons.Lesson lesson = new com.example.course_app.entity.lessons.Lesson();
        lesson.setTitle("Тестовый урок");
        lesson.setContent("Содержание тестового урока");
        lesson.setCourse(course);
        lesson = lessonRepository.save(lesson);
        
        // Создаем тест
        testEntity = new com.example.course_app.entity.tests.Test();
        testEntity.setTitle("Тестовый тест");
        testEntity.setType(com.example.course_app.entity.tests.TestType.TEXT_INPUT);
        testEntity.setRequiresManualCheck(true);
        testEntity.setLesson(lesson);
        testRepository.save(testEntity);

        // Создаем вопрос
        question = new Question();
        question.setText("Тестовый вопрос");
        question.setType(QuestionType.TEXT_INPUT);
        question.setTest(testEntity);
        question.setPoints(10);
        questionRepository.save(question);

        // Создаем отправку
        submission = new StudentSubmission();
        submission.setStudent(student);
        submission.setTest(testEntity);
        submission.setStartTime(LocalDateTime.now());
        submissionRepository.save(submission);

        // Создаем ответ студента
        studentAnswer = new StudentAnswer();
        studentAnswer.setSubmission(submission);
        studentAnswer.setQuestion(question);
        studentAnswer.setAnswerText("Тестовый ответ");
        studentAnswer.setIsCorrect(false);
        studentAnswer.setScore(0);
        studentAnswerRepository.save(studentAnswer);
        
        // Генерируем JWT токены с использованием Authentication
        Authentication studentAuth = new UsernamePasswordAuthenticationToken(
                student,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        studentToken = jwtTokenProvider.generateToken(studentAuth);

        Authentication teacherAuth = new UsernamePasswordAuthenticationToken(
                teacher,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
        teacherToken = jwtTokenProvider.generateToken(teacherAuth);
    }

    /**
     * Очистка тестовых данных после каждого теста.
     */
    @AfterEach
    void tearDown() {
        studentAnswerRepository.deleteAll();
        submissionRepository.deleteAll();
        questionRepository.deleteAll();
        testRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Тест создания нового ответа студента.
     * 
     * @throws Exception если возникла ошибка при выполнении запроса
     */
    @Test
    void testCreateStudentAnswer() throws Exception {
        // Создаем новый вопрос для теста
        Question newQuestion = new Question();
        newQuestion.setText("Новый вопрос");
        newQuestion.setType(QuestionType.TEXT_INPUT);
        newQuestion.setTest(testEntity);
        questionRepository.save(newQuestion);

        // Подготовка запроса
        CreateStudentAnswerRequest request = new CreateStudentAnswerRequest(
                submission.getId(),
                newQuestion.getId(),
                "Ответ на новый вопрос"
        );

        // Выполнение запроса и проверка результата
        mockMvc.perform(post("/api/student-answers")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.submissionId", is(submission.getId().intValue())))
                .andExpect(jsonPath("$.questionId", is(newQuestion.getId().intValue())))
                .andExpect(jsonPath("$.answerText", is("Ответ на новый вопрос")))
                .andExpect(jsonPath("$.isCorrect", is(false)));
    }

    /**
     * Тест получения ответа по идентификатору.
     * 
     * @throws Exception если возникла ошибка при выполнении запроса
     */
    @Test
    void testGetStudentAnswerById() throws Exception {
        mockMvc.perform(get("/api/student-answers/{id}", studentAnswer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(studentAnswer.getId().intValue())))
                .andExpect(jsonPath("$.submissionId", is(submission.getId().intValue())))
                .andExpect(jsonPath("$.questionId", is(question.getId().intValue())))
                .andExpect(jsonPath("$.questionText", is("Тестовый вопрос")))
                .andExpect(jsonPath("$.answerText", is("Тестовый ответ")))
                .andExpect(jsonPath("$.isCorrect", is(false)));
    }

    /**
     * Тест получения всех ответов для конкретной отправки.
     * 
     * @throws Exception если возникла ошибка при выполнении запроса
     */
    @Test
    void testGetAnswersBySubmissionId() throws Exception {
        mockMvc.perform(get("/api/student-answers/by-submission/{submissionId}", submission.getId())
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(studentAnswer.getId().intValue())))
                .andExpect(jsonPath("$[0].submissionId", is(submission.getId().intValue())))
                .andExpect(jsonPath("$[0].questionId", is(question.getId().intValue())))
                .andExpect(jsonPath("$[0].answerText", is("Тестовый ответ")));
    }

    /**
     * Тест получения всех ответов на конкретный вопрос.
     * 
     * @throws Exception если возникла ошибка при выполнении запроса
     */
    @Test
    void testGetAnswersByQuestionId() throws Exception {
        mockMvc.perform(get("/api/student-answers/by-question/{questionId}", question.getId())
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(studentAnswer.getId().intValue())))
                .andExpect(jsonPath("$[0].submissionId", is(submission.getId().intValue())))
                .andExpect(jsonPath("$[0].questionId", is(question.getId().intValue())))
                .andExpect(jsonPath("$[0].answerText", is("Тестовый ответ")));
    }

    /**
     * Тест получения ответа по отправке и вопросу.
     * 
     * @throws Exception если возникла ошибка при выполнении запроса
     */
    @Test
    void testGetAnswerBySubmissionAndQuestion() throws Exception {
        mockMvc.perform(get("/api/student-answers/by-submission/{submissionId}/question/{questionId}",
                submission.getId(), question.getId())
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(studentAnswer.getId().intValue())))
                .andExpect(jsonPath("$.submissionId", is(submission.getId().intValue())))
                .andExpect(jsonPath("$.questionId", is(question.getId().intValue())))
                .andExpect(jsonPath("$.answerText", is("Тестовый ответ")));
    }

    /**
     * Тест обновления текста ответа.
     * 
     * @throws Exception если возникла ошибка при выполнении запроса
     */
    @Test
    void testUpdateAnswerText() throws Exception {
        // Подготовка запроса
        UpdateStudentAnswerRequest request = new UpdateStudentAnswerRequest("Обновленный ответ");

        // Выполнение запроса и проверка результата
        mockMvc.perform(put("/api/student-answers/{id}", studentAnswer.getId())
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(studentAnswer.getId().intValue())))
                .andExpect(jsonPath("$.answerText", is("Обновленный ответ")));
    }

    /**
     * Тест оценки ответа студента (для преподавателя).
     * 
     * @throws Exception если возникла ошибка при выполнении запроса
     */
    @Test
    void testGradeStudentAnswer() throws Exception {
        // Подготовка запроса
        GradeStudentAnswerRequest request = new GradeStudentAnswerRequest(true, 10);

        // Выполнение запроса и проверка результата
        mockMvc.perform(put("/api/student-answers/{id}/grade", studentAnswer.getId())
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(studentAnswer.getId().intValue())))
                .andExpect(jsonPath("$.isCorrect", is(true)))
                .andExpect(jsonPath("$.score", is(10)));
    }

    /**
     * Тест удаления ответа.
     * 
     * @throws Exception если возникла ошибка при выполнении запроса
     */
    @Test
    void testDeleteStudentAnswer() throws Exception {
        // Выполнение запроса и проверка результата
        mockMvc.perform(delete("/api/student-answers/{id}", studentAnswer.getId())
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNoContent());

        // Проверка, что ответ действительно удален
        mockMvc.perform(get("/api/student-answers/{id}", studentAnswer.getId())
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNotFound());
    }

    /**
     * Тест проверки правильности ответа.
     * 
     * @throws Exception если возникла ошибка при выполнении запроса
     */
    @Test
    void testCheckAnswerCorrectness() throws Exception {
        // Выполнение запроса и проверка результата
        mockMvc.perform(post("/api/student-answers/{id}/check", studentAnswer.getId())
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
    }
}
