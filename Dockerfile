# 1. Mərhələ: Build (Maven istifadə edərək layihəni yığırıq)
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Mərhələ: Run (Yalnız işlək hissəni götürürük)
# OpenJDK əvəzinə daha stabil olan Eclipse Temurin istifadə edirik
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]