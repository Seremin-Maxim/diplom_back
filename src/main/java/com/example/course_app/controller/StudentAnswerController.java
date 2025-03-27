package com.example.course_app.controller;

import com.example.course_app.dto.CreateStudentAnswerRequest;
import com.example.course_app.dto.GradeStudentAnswerRequest;
import com.example.course_app.dto.StudentAnswerDTO;
import com.example.course_app.dto.UpdateStudentAnswerRequest;
import com.example.course_app.entity.questions.Question;
import com.example.course_app.entity.submissions.StudentAnswer;
import com.example.course_app.service.submissions.StudentAnswerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с ответами студентов на вопросы.
 * Предоставляет API для создания, получения, обновления и удаления ответов студентов.
 */
@RestController
@RequestMapping("/api/student-answers")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class StudentAnswerController {

    private final StudentAnswerService studentAnswerService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param studentAnswerService сервис для работы с ответами студентов
     */
    @Autowired
    public StudentAnswerController(StudentAnswerService studentAnswerService) {
        this.studentAnswerService = studentAnswerService;
    }

    /**
     * Создать новый ответ студента.
     *
     * @param request данные для создания ответа
     * @return созданный ответ
     */
    @PostMapping
    public ResponseEntity<StudentAnswerDTO> createStudentAnswer(@Valid @RequestBody CreateStudentAnswerRequest request) {
        StudentAnswer answer = studentAnswerService.createStudentAnswer(
                request.getSubmissionId(),
                request.getQuestionId(),
                request.getAnswerText()
        );
        return new ResponseEntity<>(convertToDTO(answer), HttpStatus.CREATED);
    }

    /**
     * Получить ответ по идентификатору.
     *
     * @param id идентификатор ответа
     * @return ответ или статус 404, если ответ не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentAnswerDTO> getStudentAnswerById(@PathVariable Long id) {
        return studentAnswerService.getStudentAnswerById(id)
                .map(answer -> new ResponseEntity<>(convertToDTO(answer), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Получить все ответы для конкретной отправки.
     *
     * @param submissionId идентификатор отправки
     * @return список ответов
     */
    @GetMapping("/by-submission/{submissionId}")
    public ResponseEntity<List<StudentAnswerDTO>> getAnswersBySubmissionId(@PathVariable Long submissionId) {
        List<StudentAnswer> answers = studentAnswerService.getAnswersBySubmissionId(submissionId);
        List<StudentAnswerDTO> answerDTOs = answers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(answerDTOs, HttpStatus.OK);
    }

    /**
     * Получить все ответы на конкретный вопрос.
     *
     * @param questionId идентификатор вопроса
     * @return список ответов
     */
    @GetMapping("/by-question/{questionId}")
    public ResponseEntity<List<StudentAnswerDTO>> getAnswersByQuestionId(@PathVariable Long questionId) {
        List<StudentAnswer> answers = studentAnswerService.getAnswersByQuestionId(questionId);
        List<StudentAnswerDTO> answerDTOs = answers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(answerDTOs, HttpStatus.OK);
    }

    /**
     * Получить ответ студента на конкретный вопрос в конкретной отправке.
     *
     * @param submissionId идентификатор отправки
     * @param questionId идентификатор вопроса
     * @return ответ или статус 404, если ответ не найден
     */
    @GetMapping("/by-submission/{submissionId}/question/{questionId}")
    public ResponseEntity<StudentAnswerDTO> getAnswerBySubmissionAndQuestion(
            @PathVariable Long submissionId,
            @PathVariable Long questionId) {
        StudentAnswer answer = studentAnswerService.getAnswerBySubmissionIdAndQuestionId(submissionId, questionId);
        if (answer == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(convertToDTO(answer), HttpStatus.OK);
    }

    /**
     * Обновить текст ответа.
     *
     * @param id идентификатор ответа
     * @param request данные для обновления
     * @return обновленный ответ или статус 404, если ответ не найден
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentAnswerDTO> updateAnswerText(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentAnswerRequest request) {
        try {
            StudentAnswer updatedAnswer = studentAnswerService.updateAnswerText(id, request.getAnswerText());
            return new ResponseEntity<>(convertToDTO(updatedAnswer), HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Оценить ответ студента (для преподавателя).
     *
     * @param id идентификатор ответа
     * @param request данные для оценки
     * @return обновленный ответ или статус 404, если ответ не найден
     */
    @PutMapping("/{id}/grade")
    public ResponseEntity<StudentAnswerDTO> gradeStudentAnswer(
            @PathVariable Long id,
            @Valid @RequestBody GradeStudentAnswerRequest request) {
        try {
            StudentAnswer answer = studentAnswerService.getStudentAnswerById(id)
                    .orElseThrow(() -> new IllegalStateException("Ответ с ID " + id + " не найден"));
            
            // Устанавливаем правильность ответа
            answer = studentAnswerService.markAnswerAsCorrect(id, request.getIsCorrect());
            
            // Устанавливаем оценку
            answer.setScore(request.getScore());
            
            return new ResponseEntity<>(convertToDTO(answer), HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Удалить ответ.
     *
     * @param id идентификатор ответа
     * @return статус 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudentAnswer(@PathVariable Long id) {
        try {
            studentAnswerService.deleteStudentAnswer(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Проверить ответ студента на правильность (автоматическая проверка).
     *
     * @param id идентификатор ответа
     * @return результат проверки
     */
    @PostMapping("/{id}/check")
    public ResponseEntity<Boolean> checkAnswerCorrectness(@PathVariable Long id) {
        try {
            boolean isCorrect = studentAnswerService.checkAnswerCorrectness(id);
            return new ResponseEntity<>(isCorrect, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Преобразует сущность StudentAnswer в DTO.
     *
     * @param answer сущность ответа студента
     * @return DTO ответа студента
     */
    private StudentAnswerDTO convertToDTO(StudentAnswer answer) {
        Question question = answer.getQuestion();
        
        return StudentAnswerDTO.builder()
                .id(answer.getId())
                .submissionId(answer.getSubmission().getId())
                .questionId(question.getId())
                .questionText(question.getText())
                .answerText(answer.getAnswerText())
                .isCorrect(answer.getIsCorrect())
                .score(answer.getScore())
                .createdAt(answer.getCreatedAt())
                .updatedAt(answer.getUpdatedAt())
                .build();
    }
}
