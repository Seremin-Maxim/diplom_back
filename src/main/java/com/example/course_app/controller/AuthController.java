package com.example.course_app.controller;

import com.example.course_app.dto.AuthResponse;
import com.example.course_app.entity.Role;
import com.example.course_app.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.course_app.service.UserService;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> signUpRequest) {
        try {
            // Создаем нового пользователя
            User user = new User();
            user.setEmail(signUpRequest.get("email"));
            user.setPassword(signUpRequest.get("password"));
            user.setFirstName(signUpRequest.get("firstName"));
            user.setLastName(signUpRequest.get("lastName"));
            
            // Устанавливаем роль из запроса
            String role = signUpRequest.get("role");
            if (role != null && role.equals("TEACHER")) {
                user.setRole(Role.TEACHER);
            }
            
            // Регистрируем пользователя
            User registeredUser = userService.registerUser(user);
            System.out.println("User registered successfully: " + registeredUser.getEmail());
            
            return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
        } catch (Exception e) {
            System.out.println("Error in signup: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping({"/signin", "/login"})
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        try {
            System.out.println("Attempting to authenticate user: " + loginRequest.get("email"));
            
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
            }

            
            // Аутентифицируем пользователя и получаем ответ
            AuthResponse authResponse = userService.authenticateUser(
                loginRequest.get("email"), 
                loginRequest.get("password")
            );
            
            System.out.println("Authentication successful for user: " + authResponse.getEmail());
            
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            System.out.println("Error in signin: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}