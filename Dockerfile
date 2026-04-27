# 1. Mərhələ: Build
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Mərhələ: Run
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Port
EXPOSE 8080

# Pulsuz servislərdə RAM limitinə (512MB) düşməmək üçün JVM optimallaşdırması
# -Xmx300M: Maksimum 300MB RAM istifadə etsin (qalan hissə sistem üçün qalsın)
ENV JAVA_OPTS="-Xmx300M -Xss512K"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]