package com.example.course_app.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.test.context.support.TestPropertySourceUtils;

/**
 * Инициализатор тестового окружения.
 * Устанавливает необходимые свойства для тестов.
 */
public class TestEnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     * Инициализирует контекст приложения для тестов.
     * Устанавливает необходимые свойства, если они не заданы.
     *
     * @param applicationContext контекст приложения
     */
    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        // Устанавливаем свойства для тестов
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=password",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=true",
                "spring.flyway.enabled=false",
                "jwt.secret=testSecretKeyForJWTtestSecretKeyForJWTtestSecretKeyForJWT",
                "jwt.expiration=3600000",
                "spring.mvc.cors.allowed-origins=http://localhost:3000",
                "spring.mvc.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS",
                "spring.mvc.cors.allowed-headers=*",
                "spring.mvc.cors.allow-credentials=true"
        );
    }
}
