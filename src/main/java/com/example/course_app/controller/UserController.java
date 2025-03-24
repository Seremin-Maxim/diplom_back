package com.example.course_app.controller;

import com.example.course_app.dto.UserProfileResponse;
import com.example.course_app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.course_app.entity.User;
import com.example.course_app.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }



    /**
     * Получение профиля текущего аутентифицированного пользователя
     * @return Данные профиля пользователя
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        // Ищем пользователя в базе данных
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Создаем объект ответа
        Map<String, Object> response = new HashMap<>();
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        
        return ResponseEntity.ok(response);
    }




    /**
     * Получение профиля пользователя по ID
     * Доступ только для аутентифицированных пользователей
     * @param id ID пользователя
     * @return Данные профиля пользователя
     */
    @GetMapping("/profile/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long id) {
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // String email = authentication.getName();
        
        // // Ищем пользователя в базе данных
        // User user = userRepository.findByEmail(email)
        //         .orElseThrow(() -> new RuntimeException("User not found"));
        
        // // Создаем объект ответа
        // Map<String, Object> response = new HashMap<>();
        // response.put("email", user.getEmail());
        // response.put("firstName", user.getFirstName());
        // response.put("lastName", user.getLastName());



        return ResponseEntity.ok(userService.getUserProfile(id));
    }
}
