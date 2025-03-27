package com.example.course_app.controller;

import com.example.course_app.dto.CreateStudentAnswerRequest;
import com.example.course_app.dto.GradeStudentAnswerRequest;
import com.example.course_app.dto.UpdateStudentAnswerRequest;
import com.example.course_app.entity.questions.Question;
import com.example.course_app.entity.submissions.StudentAnswer;
import com.example.course_app.entity.submissions.StudentSubmission;
import com.example.course_app.service.submissions.StudentAnswerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для контроллера ответов студентов.
 */
@WebMvcTest(StudentAnswerController.class)
@WithMockUser(username="test@example.com", roles={"USER", "ADMIN"})
public class StudentAnswerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentAnswerService studentAnswerService;

    @Autowired
    private ObjectMapper objectMapper;

    private StudentAnswer studentAnswer;
    private StudentSubmission submission;
    private Question question;

    /**
     * Настройка тестовых данных перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        // Создаем тестовые объекты
        submission = new StudentSubmission();
        submission.setId(1L);

        question = new Question();
        question.setId(1L);
        question.setText("Тестовый вопрос");

        studentAnswer = new StudentAnswer();
        studentAnswer.setId(1L);
        studentAnswer.setSubmission(submission);
        studentAnswer.setQuestion(question);
        studentAnswer.setAnswerText("Тестовый ответ");
        studentAnswer.setIsCorrect(false);
        studentAnswer.setScore(0);
        studentAnswer.setCreatedAt(LocalDateTime.now());
        studentAnswer.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Тест создания нового ответа студента.
     */
    @Test
    void testCreateStudentAnswer() throws Exception {
        // Подготовка запроса
        CreateStudentAnswerRequest request = new CreateStudentAnswerRequest(1L, 1L, "Тестовый ответ");

        // Настройка мока сервиса
        when(studentAnswerService.createStudentAnswer(anyLong(), anyLong(), anyString())).thenReturn(studentAnswer);

        // Выполнение запроса и проверка результата
        mockMvc.perform(post("/api/student-answers")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.submissionId", is(1)))
                .andExpect(jsonPath("$.questionId", is(1)))
                .andExpect(jsonPath("$.answerText", is("Тестовый ответ")));

        // Проверка вызова сервиса
        verify(studentAnswerService).createStudentAnswer(1L, 1L, "Тестовый ответ");
    }

    /**
     * Тест получения ответа по идентификатору.
     */
    @Test
    void testGetStudentAnswerById() throws Exception {
        // Настройка мока сервиса
        when(studentAnswerService.getStudentAnswerById(1L)).thenReturn(Optional.of(studentAnswer));

        // Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/student-answers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.submissionId", is(1)))
                .andExpect(jsonPath("$.questionId", is(1)))
                .andExpect(jsonPath("$.questionText", is("Тестовый вопрос")))
                .andExpect(jsonPath("$.answerText", is("Тестовый ответ")));

        // Проверка вызова сервиса
        verify(studentAnswerService).getStudentAnswerById(1L);
    }

    /**
     * Тест получения ответа по несуществующему идентификатору.
     */
    @Test
    void testGetStudentAnswerByIdNotFound() throws Exception {
        // Настройка мока сервиса
        when(studentAnswerService.getStudentAnswerById(999L)).thenReturn(Optional.empty());

        // Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/student-answers/999"))
                .andExpect(status().isNotFound());

        // Проверка вызова сервиса
        verify(studentAnswerService).getStudentAnswerById(999L);
    }

    /**
     * Тест получения всех ответов для конкретной отправки.
     */
    @Test
    void testGetAnswersBySubmissionId() throws Exception {
        // Создаем список ответов
        List<StudentAnswer> answers = Arrays.asList(studentAnswer);

        // Настройка мока сервиса
        when(studentAnswerService.getAnswersBySubmissionId(1L)).thenReturn(answers);

        // Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/student-answers/by-submission/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].submissionId", is(1)))
                .andExpect(jsonPath("$[0].questionId", is(1)))
                .andExpect(jsonPath("$[0].answerText", is("Тестовый ответ")));

        // Проверка вызова сервиса
        verify(studentAnswerService).getAnswersBySubmissionId(1L);
    }

    /**
     * Тест получения всех ответов на конкретный вопрос.
     */
    @Test
    void testGetAnswersByQuestionId() throws Exception {
        // Создаем список ответов
        List<StudentAnswer> answers = Arrays.asList(studentAnswer);

        // Настройка мока сервиса
        when(studentAnswerService.getAnswersByQuestionId(1L)).thenReturn(answers);

        // Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/student-answers/by-question/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].submissionId", is(1)))
                .andExpect(jsonPath("$[0].questionId", is(1)))
                .andExpect(jsonPath("$[0].answerText", is("Тестовый ответ")));

        // Проверка вызова сервиса
        verify(studentAnswerService).getAnswersByQuestionId(1L);
    }

    /**
     * Тест обновления текста ответа.
     */
    @Test
    void testUpdateAnswerText() throws Exception {
        // Подготовка запроса
        UpdateStudentAnswerRequest request = new UpdateStudentAnswerRequest("Обновленный ответ");

        // Создаем обновленный ответ
        StudentAnswer updatedAnswer = new StudentAnswer();
        updatedAnswer.setId(1L);
        updatedAnswer.setSubmission(submission);
        updatedAnswer.setQuestion(question);
        updatedAnswer.setAnswerText("Обновленный ответ");
        updatedAnswer.setIsCorrect(false);
        updatedAnswer.setScore(0);
        updatedAnswer.setCreatedAt(studentAnswer.getCreatedAt());
        updatedAnswer.setUpdatedAt(LocalDateTime.now());

        // Настройка мока сервиса
        when(studentAnswerService.updateAnswerText(1L, "Обновленный ответ")).thenReturn(updatedAnswer);

        // Выполнение запроса и проверка результата
        mockMvc.perform(put("/api/student-answers/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.answerText", is("Обновленный ответ")));

        // Проверка вызова сервиса
        verify(studentAnswerService).updateAnswerText(1L, "Обновленный ответ");
    }

    /**
     * Тест оценки ответа студента.
     */
    @Test
    void testGradeStudentAnswer() throws Exception {
        // Подготовка запроса
        GradeStudentAnswerRequest request = new GradeStudentAnswerRequest(true, 10);

        // Создаем оцененный ответ
        StudentAnswer gradedAnswer = new StudentAnswer();
        gradedAnswer.setId(1L);
        gradedAnswer.setSubmission(submission);
        gradedAnswer.setQuestion(question);
        gradedAnswer.setAnswerText("Тестовый ответ");
        gradedAnswer.setIsCorrect(true);
        gradedAnswer.setScore(10);
        gradedAnswer.setCreatedAt(studentAnswer.getCreatedAt());
        gradedAnswer.setUpdatedAt(LocalDateTime.now());

        // Настройка мока сервиса
        when(studentAnswerService.getStudentAnswerById(1L)).thenReturn(Optional.of(studentAnswer));
        when(studentAnswerService.markAnswerAsCorrect(1L, true)).thenReturn(gradedAnswer);

        // Выполнение запроса и проверка результата
        mockMvc.perform(put("/api/student-answers/1/grade")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.isCorrect", is(true)))
                .andExpect(jsonPath("$.score", is(10)));

        // Проверка вызова сервиса
        verify(studentAnswerService).getStudentAnswerById(1L);
        verify(studentAnswerService).markAnswerAsCorrect(1L, true);
    }

    /**
     * Тест удаления ответа.
     */
    @Test
    void testDeleteStudentAnswer() throws Exception {
        // Настройка мока сервиса
        doNothing().when(studentAnswerService).deleteStudentAnswer(1L);

        // Выполнение запроса и проверка результата
        mockMvc.perform(delete("/api/student-answers/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        // Проверка вызова сервиса
        verify(studentAnswerService).deleteStudentAnswer(1L);
    }

    /**
     * Тест проверки правильности ответа.
     */
    @Test
    void testCheckAnswerCorrectness() throws Exception {
        // Настройка мока сервиса
        when(studentAnswerService.checkAnswerCorrectness(1L)).thenReturn(true);

        // Выполнение запроса и проверка результата
        mockMvc.perform(post("/api/student-answers/1/check")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Проверка вызова сервиса
        verify(studentAnswerService).checkAnswerCorrectness(1L);
    }
}
