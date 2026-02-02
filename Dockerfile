FROM maven:3.9.6-eclipse-temurin-11-alpine AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:11-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /app/target/*.jar app.jar

RUN chown spring:spring app.jar
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]