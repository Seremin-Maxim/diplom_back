package com.example.course_app.controller;

import com.example.course_app.dto.StudentAnswerDTO;
import com.example.course_app.dto.SubmissionDTO;
import com.example.course_app.dto.SubmissionRequest;
import com.example.course_app.entity.User;
import com.example.course_app.entity.questions.Question;
import com.example.course_app.entity.submissions.StudentAnswer;
import com.example.course_app.entity.submissions.StudentSubmission;
import com.example.course_app.service.UserService;
import com.example.course_app.service.questions.QuestionService;
import com.example.course_app.service.submissions.StudentAnswerService;
import com.example.course_app.service.submissions.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с отправками ответов на тесты.
 * Предоставляет API для создания, получения и управления отправками ответов студентов.
 */
@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final StudentAnswerService studentAnswerService;
    private final UserService userService;
    private final QuestionService questionService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param submissionService сервис для работы с отправками
     * @param studentAnswerService сервис для работы с ответами студентов
     * @param userService сервис для работы с пользователями
     * @param questionService сервис для работы с вопросами
     */
    @Autowired
    public SubmissionController(
            SubmissionService submissionService,
            StudentAnswerService studentAnswerService,
            UserService userService,
            QuestionService questionService) {
        this.submissionService = submissionService;
        this.studentAnswerService = studentAnswerService;
        this.userService = userService;
        this.questionService = questionService;
    }

    /**
     * Создать новую отправку ответов на тест.
     *
     * @param request данные для создания отправки
     * @return созданная отправка
     */
    @PostMapping
    public ResponseEntity<?> createSubmission(@Valid @RequestBody SubmissionRequest request) {
        try {
            // Получаем текущего пользователя
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            
            System.out.println("Пользователь: " + username + ", ID: " + user.getId());
            System.out.println("Полученный запрос: testId=" + request.getTestId() + ", количество ответов=" + request.getAnswers().size());
            
            // Создаем новую отправку
            StudentSubmission submission = submissionService.createSubmission(user.getId(), request.getTestId());
            System.out.println("Создана отправка с ID: " + submission.getId());
            
            // Обрабатываем ответы на вопросы
            for (SubmissionRequest.AnswerRequest answerRequest : request.getAnswers()) {
                try {
                    System.out.println("Обработка ответа на вопрос ID: " + answerRequest.getQuestionId());
                    
                    Optional<Question> questionOpt = questionService.getQuestionById(answerRequest.getQuestionId());
                    if (questionOpt.isEmpty()) {
                        System.out.println("Вопрос с ID " + answerRequest.getQuestionId() + " не найден");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Вопрос с ID " + answerRequest.getQuestionId() + " не найден");
                    }
                    
                    Question question = questionOpt.get();
                    System.out.println("Тип вопроса: " + question.getType());
                    
                    // Создаем ответ студента
                    StudentAnswer answer;
                    
                    if ("TEXT".equals(question.getType().toString())) {
                        // Для текстовых вопросов
                        System.out.println("Создание текстового ответа: " + answerRequest.getTextAnswer());
                        answer = studentAnswerService.createStudentAnswer(
                                submission.getId(),
                                question.getId(),
                                answerRequest.getTextAnswer()
                        );
                    } else {
                        // Для вопросов с выбором (одиночным или множественным)
                        System.out.println("Создание ответа с выбором: " + answerRequest.getSelectedAnswerIds());
                        
                        if (answerRequest.getSelectedAnswerIds() == null || answerRequest.getSelectedAnswerIds().isEmpty()) {
                            System.out.println("Не указаны варианты ответа для вопроса " + question.getId());
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body("Не указаны варианты ответа для вопроса " + question.getId());
                        }
                        
                        answer = studentAnswerService.createStudentAnswerWithSelectedOptions(
                                submission.getId(),
                                question.getId(),
                                answerRequest.getSelectedAnswerIds()
                        );
                    }
                    
                    // Проверяем правильность ответа
                    studentAnswerService.checkAnswerCorrectness(answer.getId());
                    System.out.println("Ответ на вопрос " + question.getId() + " успешно сохранен");
                } catch (Exception e) {
                    System.out.println("Ошибка при обработке ответа на вопрос " + answerRequest.getQuestionId() + ": " + e.getMessage());
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Ошибка при обработке ответа на вопрос " + answerRequest.getQuestionId() + ": " + e.getMessage());
                }
            }
            
            // Завершаем отправку
            submission = submissionService.completeSubmission(submission.getId());
            System.out.println("Отправка завершена");
            
            // Рассчитываем итоговую оценку
            submissionService.calculateSubmissionScore(submission.getId());
            System.out.println("Итоговая оценка рассчитана");
            
            // Возвращаем результат
            return ResponseEntity.ok(convertToDTO(submission));
        } catch (Exception e) {
            System.out.println("Ошибка при создании отправки: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Ошибка при создании отправки: " + e.getMessage());
        }
    }

    /**
     * Получить отправку по идентификатору.
     *
     * @param id идентификатор отправки
     * @return отправка или статус 404, если отправка не найдена
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubmissionDTO> getSubmissionById(@PathVariable Long id) {
        // Получаем текущего пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        Optional<StudentSubmission> submissionOpt = submissionService.getSubmissionById(id);
        
        if (submissionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        StudentSubmission submission = submissionOpt.get();
        
        // Проверяем, принадлежит ли отправка текущему пользователю или пользователь является преподавателем/админом
        if (!submission.getStudent().getId().equals(user.getId()) && 
                !user.getRole().equals("TEACHER") && !user.getRole().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(convertToDTO(submission));
    }

    /**
     * Получить все отправки текущего пользователя.
     *
     * @return список отправок
     */
    @GetMapping("/my")
    public ResponseEntity<List<SubmissionDTO>> getMySubmissions() {
        // Получаем текущего пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        List<StudentSubmission> submissions = submissionService.getSubmissionsByStudentId(user.getId());
        List<SubmissionDTO> submissionDTOs = submissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(submissionDTOs);
    }

    /**
     * Получить все отправки для конкретного теста.
     *
     * @param testId идентификатор теста
     * @return список отправок
     */
    @GetMapping("/test/{testId}")
    public ResponseEntity<List<SubmissionDTO>> getSubmissionsByTestId(@PathVariable Long testId) {
        // Получаем текущего пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        // Проверяем, является ли пользователь преподавателем или админом
        if (!user.getRole().equals("TEACHER") && !user.getRole().equals("ADMIN")) {
            // Если обычный пользователь, возвращаем только его отправки для этого теста
            List<StudentSubmission> submissions = submissionService.getSubmissionsByStudentIdAndTestId(user.getId(), testId);
            List<SubmissionDTO> submissionDTOs = submissions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(submissionDTOs);
        } else {
            // Если преподаватель или админ, возвращаем все отправки для теста
            List<StudentSubmission> submissions = submissionService.getSubmissionsByTestId(testId);
            List<SubmissionDTO> submissionDTOs = submissions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(submissionDTOs);
        }
    }

    /**
     * Получить ответы для конкретной отправки.
     *
     * @param submissionId идентификатор отправки
     * @return список ответов
     */
    @GetMapping("/{submissionId}/answers")
    public ResponseEntity<List<StudentAnswerDTO>> getAnswersBySubmissionId(@PathVariable Long submissionId) {
        // Получаем текущего пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        // Проверяем, существует ли отправка
        Optional<StudentSubmission> submissionOpt = submissionService.getSubmissionById(submissionId);
        
        if (submissionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        StudentSubmission submission = submissionOpt.get();
        
        // Проверяем, принадлежит ли отправка текущему пользователю или пользователь является преподавателем/админом
        if (!submission.getStudent().getId().equals(user.getId()) && 
                !user.getRole().equals("TEACHER") && !user.getRole().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<StudentAnswer> answers = studentAnswerService.getAnswersBySubmissionId(submissionId);
        List<StudentAnswerDTO> answerDTOs = answers.stream()
                .map(this::convertAnswerToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(answerDTOs);
    }

    /**
     * Преобразует сущность StudentSubmission в DTO.
     * Использует только идентификаторы для избежания ошибок LazyInitializationException.
     *
     * @param submission сущность отправки
     * @return DTO отправки
     */
    private SubmissionDTO convertToDTO(StudentSubmission submission) {
        // Получаем идентификаторы студента и теста
        Long studentId = submission.getStudent().getId();
        Long testId = submission.getTest().getId();
        
        // Получаем данные студента
        User student = null;
        try {
            student = userService.findByUsername(submission.getStudent().getEmail());
        } catch (Exception e) {
            System.out.println("Не удалось получить данные студента: " + e.getMessage());
        }
        
        String studentName = "";
        if (student != null) {
            studentName = student.getFirstName() + " " + student.getLastName();
        }
        
        String testTitle = "";
        try {
            testTitle = submission.getTest().getTitle();
        } catch (Exception e) {
            // Если не удалось получить название теста, используем пустую строку
            System.out.println("Не удалось получить название теста: " + e.getMessage());
        }
        
        // Создаем DTO
        return SubmissionDTO.builder()
                .id(submission.getId())
                .studentId(studentId)
                .studentName(studentName)
                .testId(testId)
                .testTitle(testTitle)
                .startTime(submission.getStartTime())
                .endTime(submission.getEndTime())
                .score(submission.getScore())
                .maxScore(calculateMaxScore(testId))
                .reviewed(submission.getReviewed())
                .build();
    }

    /**
     * Преобразует сущность StudentAnswer в DTO.
     *
     * @param answer сущность ответа студента
     * @return DTO ответа студента
     */
    private StudentAnswerDTO convertAnswerToDTO(StudentAnswer answer) {
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

    /**
     * Рассчитывает максимально возможную оценку за тест.
     *
     * @param testId идентификатор теста
     * @return максимальная оценка
     */
    private int calculateMaxScore(Long testId) {
        List<Question> questions = questionService.getQuestionsByTestId(testId);
        return questions.stream()
                .mapToInt(Question::getPoints)
                .sum();
    }
}
