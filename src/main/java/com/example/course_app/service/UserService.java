package com.example.course_app.service;

import com.example.course_app.dto.AuthResponse;
import com.example.course_app.dto.UserProfileResponse;
import com.example.course_app.entity.Role;
import com.example.course_app.entity.User;
import com.example.course_app.repository.UserRepository;
import com.example.course_app.config.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      @Lazy AuthenticationManager authenticationManager,
                      JwtTokenProvider jwtTokenProvider) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository cannot be null");
        }
        if (passwordEncoder == null) {
            throw new IllegalArgumentException("passwordEncoder cannot be null");
        }
        if (authenticationManager == null) {
            throw new IllegalArgumentException("authenticationManager cannot be null");
        }
        if (jwtTokenProvider == null) {
            throw new IllegalArgumentException("jwtTokenProvider cannot be null");
        }
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }
        
        // Устанавливаем роль USER только если роль не была установлена ранее
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public AuthResponse authenticateUser(String email, String password) {
        try {
            // Проверка инициализации
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be empty");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password cannot be empty");
            }
            if (userRepository == null) {
                throw new RuntimeException("OOOOOOOOOOOOOOOOOOOOOOOOO userRepository is null");
            }
            if (authenticationManager == null) {
                throw new RuntimeException("authenticationManager is null");
            }
            if (jwtTokenProvider == null) {
                throw new RuntimeException("jwtTokenProvider is null");
            }
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);
            
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found ooooooooooooooooooo"));
            
            return new AuthResponse(jwt, user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    /**
 * Получение профиля пользователя по ID
 * @param id ID пользователя
 * @return Данные профиля пользователя
 * @throws RuntimeException если пользователь не найден
 */
public UserProfileResponse getUserProfile(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    
    return new UserProfileResponse(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName()
    );
}

/**
 * Получение пользователя по имени пользователя (email)
 * @param username имя пользователя (email)
 * @return пользователь
 * @throws UsernameNotFoundException если пользователь не найден
 */
public User findByUsername(String username) {
    return userRepository.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
}
}