package com.example.course_app.controller;

import com.example.course_app.dto.TestAccessDTO;
import com.example.course_app.dto.TestDTO;
import com.example.course_app.dto.TestMapper;
import com.example.course_app.entity.User;
import com.example.course_app.entity.tests.Test;
import com.example.course_app.service.UserService;
import com.example.course_app.service.tests.TestAccessService;
import com.example.course_app.service.tests.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для управления доступом к тестам.
 */
@RestController
@RequestMapping("/api/tests/access")
public class TestAccessController {

    private final TestService testService;
    private final TestAccessService testAccessService;
    private final UserService userService;

    /**
     * Конструктор контроллера.
     *
     * @param testService сервис для работы с тестами
     * @param testAccessService сервис для управления доступом к тестам
     * @param userService сервис для работы с пользователями
     */
    @Autowired
    public TestAccessController(
            TestService testService,
            TestAccessService testAccessService,
            UserService userService) {
        this.testService = testService;
        this.testAccessService = testAccessService;
        this.userService = userService;
    }

    /**
     * Получить список доступных тестов для текущего пользователя в уроке.
     *
     * @param lessonId идентификатор урока
     * @return список доступных тестов
     */
    @GetMapping("/lesson/{lessonId}/available-tests")
    public ResponseEntity<List<TestAccessDTO>> getAvailableTestsForCurrentUser(@PathVariable Long lessonId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        // Получаем список доступных тестов для студента
        List<Test> tests = testAccessService.getAvailableTestsForStudent(user.getId(), lessonId);
        
        // Если тесты не найдены, возвращаем пустой список вместо ошибки 403
        if (tests.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        // Формируем список доступных тестов с токенами доступа
        List<TestAccessDTO> accessDTOs = tests.stream()
                .map(test -> testAccessService.createTestAccessToken(user.getId(), test))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(accessDTOs);
    }
    
    /**
     * Получить доступ к тесту по токену.
     *
     * @param testId идентификатор теста
     * @param token токен доступа
     * @return данные теста
     */
    @GetMapping("/{testId}")
    public ResponseEntity<?> accessTestByToken(
            @PathVariable Long testId,
            @RequestParam("token") String token) {
        
        // Проверяем токен доступа
        Optional<TestAccessDTO> accessDTOOpt = testAccessService.validateTestAccessToken(token, testId);
        if (accessDTOOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недействительный токен доступа или срок его действия истек");
        }
        
        // Получаем данные теста
        Optional<Test> testOpt = testService.getTestById(testId);
        if (testOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Возвращаем данные теста
        Test test = testOpt.get();
        TestDTO testDTO = TestMapper.toDTO(test);
        return ResponseEntity.ok(testDTO);
    }
}
