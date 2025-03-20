FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Копируем только pom.xml для кэширования зависимостей
COPY pom.xml .
# Используем пустой проект для загрузки зависимостей
RUN mkdir -p src/main/java && \
    mvn dependency:go-offline

# Копируем исходный код
COPY src ./src

# Собираем приложение с оптимизациями
RUN mvn package -DskipTests -Dmaven.test.skip=true -Dmaven.javadoc.skip=true

# Используем минимальный образ JRE для запуска приложения
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем собранный JAR из предыдущего этапа
COPY --from=build /app/target/*.jar app.jar

# Порт, который будет открыт
EXPOSE 8080

# Оптимизация JVM для контейнеров
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=70", "-jar", "app.jar"]
