package com.example.course_app.controller;

import com.example.course_app.dto.AnswerMapper;
import com.example.course_app.entity.User;
import com.example.course_app.entity.answers.Answer;
import com.example.course_app.entity.questions.Question;
import com.example.course_app.entity.tests.Test;
import com.example.course_app.service.answers.AnswerService;
import com.example.course_app.service.questions.QuestionService;
import com.example.course_app.service.tests.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Контроллер для работы с вариантами ответов на вопросы.
 */
@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    private final AnswerService answerService;
    private final QuestionService questionService;
    private final TestService testService;

    @Autowired
    public AnswerController(
            AnswerService answerService,
            QuestionService questionService,
            TestService testService) {
        this.answerService = answerService;
        this.questionService = questionService;
        this.testService = testService;
    }

    /**
     * Получить вариант ответа по ID.
     *
     * @param id ID варианта ответа
     * @return ответ с данными варианта ответа или ошибка, если вариант ответа не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAnswerById(@PathVariable Long id) {
        Optional<Answer> answer = answerService.getAnswerById(id);
        if (answer.isPresent()) {
            return ResponseEntity.ok(AnswerMapper.toDTO(answer.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Вариант ответа с ID " + id + " не найден"));
        }
    }

    /**
     * Получить все варианты ответов для вопроса.
     *
     * @param questionId ID вопроса
     * @return список вариантов ответов для вопроса
     */
    @GetMapping("/question/{questionId}")
    public ResponseEntity<?> getAnswersByQuestionId(@PathVariable Long questionId) {
        // Проверяем существование вопроса
        if (!questionService.existsById(questionId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Вопрос с ID " + questionId + " не найден"));
        }

        List<Answer> answers = answerService.getAnswersByQuestionId(questionId);
        return ResponseEntity.ok(AnswerMapper.toDTOList(answers));
    }

    /**
     * Создать новый вариант ответа для вопроса.
     *
     * @param questionId ID вопроса
     * @param requestBody данные для создания варианта ответа
     * @param authentication данные аутентификации пользователя
     * @return созданный вариант ответа
     */
    @PostMapping("/question/{questionId}")
    public ResponseEntity<?> createAnswer(
            @PathVariable Long questionId,
            @RequestBody Map<String, Object> requestBody,
            Authentication authentication) {
        
        // Проверяем существование вопроса
        Optional<Question> questionOpt = questionService.getQuestionById(questionId);
        if (questionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Вопрос с ID " + questionId + " не найден"));
        }
        
        Question question = questionOpt.get();
        Test test = question.getTest();
        
        // Проверяем права доступа (только преподаватель может создавать варианты ответов)
        User user = (User) authentication.getPrincipal();
        if (!testService.isTestCreatedByUser(test.getId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "У вас нет прав для создания вариантов ответов для этого вопроса"));
        }
        
        // Получаем данные из запроса
        String text = (String) requestBody.get("text");
        Boolean isCorrect = (Boolean) requestBody.get("isCorrect");
        
        if (text == null || isCorrect == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Необходимо указать текст и статус правильности ответа"));
        }
        
        // Создаем вариант ответа
        Answer answer = answerService.createAnswer(questionId, text, isCorrect);
        return ResponseEntity.status(HttpStatus.CREATED).body(AnswerMapper.toDTO(answer));
    }

    /**
     * Обновить существующий вариант ответа.
     *
     * @param id ID варианта ответа
     * @param requestBody данные для обновления варианта ответа
     * @param authentication данные аутентификации пользователя
     * @return обновленный вариант ответа
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAnswer(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody,
            Authentication authentication) {
        
        // Проверяем существование варианта ответа
        Optional<Answer> answerOpt = answerService.getAnswerById(id);
        if (answerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Вариант ответа с ID " + id + " не найден"));
        }
        
        Answer answer = answerOpt.get();
        Question question = answer.getQuestion();
        Test test = question.getTest();
        
        // Проверяем права доступа (только преподаватель может обновлять варианты ответов)
        User user = (User) authentication.getPrincipal();
        if (!testService.isTestCreatedByUser(test.getId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "У вас нет прав для обновления этого варианта ответа"));
        }
        
        // Получаем данные из запроса
        String text = (String) requestBody.get("text");
        Boolean isCorrect = (Boolean) requestBody.get("isCorrect");
        
        if (text == null || isCorrect == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Необходимо указать текст и статус правильности ответа"));
        }
        
        // Обновляем вариант ответа
        Answer updatedAnswer = answerService.updateAnswer(id, text, isCorrect);
        return ResponseEntity.ok(AnswerMapper.toDTO(updatedAnswer));
    }

    /**
     * Удалить вариант ответа.
     *
     * @param id ID варианта ответа
     * @param authentication данные аутентификации пользователя
     * @return статус успешного удаления или ошибка
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnswer(
            @PathVariable Long id,
            Authentication authentication) {
        
        // Проверяем существование варианта ответа
        Optional<Answer> answerOpt = answerService.getAnswerById(id);
        if (answerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Вариант ответа с ID " + id + " не найден"));
        }
        
        Answer answer = answerOpt.get();
        Question question = answer.getQuestion();
        Test test = question.getTest();
        
        // Проверяем права доступа (только преподаватель может удалять варианты ответов)
        User user = (User) authentication.getPrincipal();
        if (!testService.isTestCreatedByUser(test.getId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "У вас нет прав для удаления этого варианта ответа"));
        }
        
        // Удаляем вариант ответа
        answerService.deleteAnswer(id);
        return ResponseEntity.ok(Map.of("message", "Вариант ответа успешно удален"));
    }

    /**
     * Создать набор вариантов ответов для вопроса с одним правильным ответом.
     *
     * @param questionId ID вопроса
     * @param requestBody данные для создания вариантов ответов
     * @param authentication данные аутентификации пользователя
     * @return созданные варианты ответов
     */
    @PostMapping("/question/{questionId}/single-choice")
    public ResponseEntity<?> createSingleChoiceAnswers(
            @PathVariable Long questionId,
            @RequestBody Map<String, Object> requestBody,
            Authentication authentication) {
        
        // Проверяем существование вопроса
        Optional<Question> questionOpt = questionService.getQuestionById(questionId);
        if (questionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Вопрос с ID " + questionId + " не найден"));
        }
        
        Question question = questionOpt.get();
        Test test = question.getTest();
        
        // Проверяем права доступа (только преподаватель может создавать варианты ответов)
        User user = (User) authentication.getPrincipal();
        if (!testService.isTestCreatedByUser(test.getId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "У вас нет прав для создания вариантов ответов для этого вопроса"));
        }
        
        // Получаем данные из запроса
        String correctAnswerText = (String) requestBody.get("correctAnswerText");
        @SuppressWarnings("unchecked")
        List<String> incorrectAnswersText = (List<String>) requestBody.get("incorrectAnswersText");
        
        if (correctAnswerText == null || incorrectAnswersText == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Необходимо указать текст правильного ответа и список текстов неправильных ответов"));
        }
        
        // Создаем варианты ответов
        List<Answer> answers = answerService.createSingleChoiceAnswers(questionId, correctAnswerText, incorrectAnswersText);
        return ResponseEntity.status(HttpStatus.CREATED).body(AnswerMapper.toDTOList(answers));
    }

    /**
     * Создать набор вариантов ответов для вопроса с несколькими правильными ответами.
     *
     * @param questionId ID вопроса
     * @param requestBody данные для создания вариантов ответов
     * @param authentication данные аутентификации пользователя
     * @return созданные варианты ответов
     */
    @PostMapping("/question/{questionId}/multiple-choice")
    public ResponseEntity<?> createMultipleChoiceAnswers(
            @PathVariable Long questionId,
            @RequestBody Map<String, Object> requestBody,
            Authentication authentication) {
        
        // Проверяем существование вопроса
        Optional<Question> questionOpt = questionService.getQuestionById(questionId);
        if (questionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Вопрос с ID " + questionId + " не найден"));
        }
        
        Question question = questionOpt.get();
        Test test = question.getTest();
        
        // Проверяем права доступа (только преподаватель может создавать варианты ответов)
        User user = (User) authentication.getPrincipal();
        if (!testService.isTestCreatedByUser(test.getId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "У вас нет прав для создания вариантов ответов для этого вопроса"));
        }
        
        // Получаем данные из запроса
        @SuppressWarnings("unchecked")
        List<String> correctAnswersText = (List<String>) requestBody.get("correctAnswersText");
        @SuppressWarnings("unchecked")
        List<String> incorrectAnswersText = (List<String>) requestBody.get("incorrectAnswersText");
        
        if (correctAnswersText == null || incorrectAnswersText == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Необходимо указать список текстов правильных ответов и список текстов неправильных ответов"));
        }
        
        // Создаем варианты ответов
        List<Answer> answers = answerService.createMultipleChoiceAnswers(questionId, correctAnswersText, incorrectAnswersText);
        return ResponseEntity.status(HttpStatus.CREATED).body(AnswerMapper.toDTOList(answers));
    }

    /**
     * Проверить, принадлежит ли вариант ответа вопросу.
     *
     * @param answerId ID варианта ответа
     * @param questionId ID вопроса
     * @return результат проверки
     */
    @GetMapping("/{answerId}/belongs-to-question/{questionId}")
    public ResponseEntity<?> isAnswerBelongsToQuestion(
            @PathVariable Long answerId,
            @PathVariable Long questionId) {
        
        try {
            boolean belongs = answerService.isAnswerBelongsToQuestion(answerId, questionId);
            return ResponseEntity.ok(Map.of("belongs", belongs));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Проверить, является ли вариант ответа правильным.
     *
     * @param id ID варианта ответа
     * @return результат проверки
     */
    @GetMapping("/{id}/is-correct")
    public ResponseEntity<?> isAnswerCorrect(@PathVariable Long id) {
        try {
            boolean isCorrect = answerService.isAnswerCorrect(id);
            return ResponseEntity.ok(Map.of("isCorrect", isCorrect));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
