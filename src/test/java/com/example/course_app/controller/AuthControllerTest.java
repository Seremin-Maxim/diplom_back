package com.example.course_app.controller;

import com.example.course_app.dto.AuthResponse;
import com.example.course_app.entity.User;
import com.example.course_app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Настраиваем ObjectMapper для игнорирования проблемных полей
        objectMapper = new ObjectMapper();
        objectMapper.addMixIn(User.class, UserMixIn.class);
        
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .build();
    }

    @Test
    @DisplayName("Регистрация пользователя")
    void registerUser_ShouldRegisterAndReturnSuccess() throws Exception {
        // Arrange
        Map<String, String> signupRequest = new HashMap<>();
        signupRequest.put("email", "test@example.com");
        signupRequest.put("password", "password123");
        signupRequest.put("firstName", "Иван");
        signupRequest.put("lastName", "Иванов");

        User registeredUser = new User();
        registeredUser.setId(1L);
        registeredUser.setEmail("test@example.com");
        registeredUser.setFirstName("Иван");
        registeredUser.setLastName("Иванов");

        when(userService.registerUser(any(User.class))).thenReturn(registeredUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }

    @Test
    @DisplayName("Аутентификация пользователя")
    void authenticateUser_ShouldAuthenticateAndReturnToken() throws Exception {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@example.com");
        loginRequest.put("password", "password123");

        AuthResponse authResponse = new AuthResponse(
            "jwt-token",
            1L,
            "test@example.com",
            "Иван",
            "Иванов"
        );

        when(userService.authenticateUser(anyString(), anyString())).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.lastName").value("Иванов"));
    }

    @Test
    @DisplayName("Аутентификация с пустым email")
    void authenticateUser_WithEmptyEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "");
        loginRequest.put("password", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email is required"));
    }

    @Test
    @DisplayName("Аутентификация с пустым паролем")
    void authenticateUser_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@example.com");
        loginRequest.put("password", "");

        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password is required"));
    }
}
