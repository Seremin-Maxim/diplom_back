FROM maven:3.9.6-eclipse-temurin-21 AS build

# Настройка кэширования Maven
ENV MAVEN_OPTS="-Dmaven.repo.local=/m2/repository"
WORKDIR /app

# Создаем каталог для кэша Maven и очищаем его
RUN mkdir -p /m2/repository && rm -rf /m2/repository/*

# Копируем настройки Maven и pom.xml
COPY settings.xml /root/.m2/
COPY pom.xml .

# Используем пустой проект для загрузки зависимостей
RUN mkdir -p src/main/java && \
    mkdir -p /root/.m2 && \
    # Загружаем только зависимости без очистки кэша
    mvn -s /root/.m2/settings.xml -B dependency:resolve dependency:resolve-plugins

# Копируем исходный код
COPY src ./src

# Собираем приложение с оптимизациями
RUN mvn -s /root/.m2/settings.xml -B package \
    -DskipTests -Dmaven.test.skip=true -Dmaven.javadoc.skip=true

# Используем минимальный образ JRE для запуска приложения
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Устанавливаем необходимые пакеты
RUN apk add --no-cache curl

# Копируем собранный JAR из предыдущего этапа
COPY --from=build /app/target/*.jar app.jar

# Порт, который будет открыт
EXPOSE 8080

# Оптимизация JVM для контейнеров
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=70", "-XX:+UseG1GC", "-XX:MinRAMPercentage=50", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
