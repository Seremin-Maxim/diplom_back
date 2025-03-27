package com.example.course_app.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Тестовая конфигурация безопасности для интеграционных тестов.
 * Отключает проверку безопасности для тестов.
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfigForIntegration {

    /**
     * Создает конфигурацию фильтров безопасности для тестов.
     * Отключает CSRF и разрешает все запросы.
     *
     * @param http объект HttpSecurity для настройки
     * @return настроенный SecurityFilterChain
     * @throws Exception если возникла ошибка при настройке
     */
    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}
