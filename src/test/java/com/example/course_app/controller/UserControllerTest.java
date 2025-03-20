package com.example.course_app.controller;

import com.example.course_app.dto.UserProfileResponse;
import com.example.course_app.entity.User;
import com.example.course_app.repository.UserRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Настраиваем ObjectMapper для игнорирования проблемных полей
        objectMapper = new ObjectMapper();
        objectMapper.addMixIn(User.class, UserMixIn.class);
        
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();
        
        // Создаем тестового пользователя
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Иван");
        testUser.setLastName("Иванов");
        testUser.setRole(com.example.course_app.entity.Role.USER);
    }

    @Test
    @DisplayName("Получение профиля текущего пользователя")
    @WithMockUser(username = "test@example.com")
    void getCurrentUserProfile_ShouldReturnUserProfile() throws Exception {
        // Arrange
        // Мокируем аутентификацию
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("test@example.com");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/api/users/profile")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user("test@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.lastName").value("Иванов"));
    }

    @Test
    @DisplayName("Получение профиля пользователя по ID")
    @WithMockUser
    void getUserProfile_ShouldReturnUserProfile() throws Exception {
        // Arrange
        UserProfileResponse profileResponse = new UserProfileResponse();
        profileResponse.setId(1L);
        profileResponse.setEmail("test@example.com");
        profileResponse.setFirstName("Иван");
        profileResponse.setLastName("Иванов");
        
        when(userService.getUserProfile(1L)).thenReturn(profileResponse);

        // Act & Assert
        mockMvc.perform(get("/api/users/profile/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user("test@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.lastName").value("Иванов"));
    }
}
