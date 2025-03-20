FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Копируем файлы pom.xml и скачиваем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходный код и собираем приложение
COPY src ./src
RUN mvn package -DskipTests

# Используем минимальный образ JRE для запуска приложения
FROM openjdk:17-jdk-slim
WORKDIR /app

# Копируем собранный JAR из предыдущего этапа
COPY --from=build /app/target/*.jar app.jar

# Порт, который будет открыт
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
