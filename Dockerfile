# ===== BUILD STAGE =====
FROM maven:3.9.8-amazoncorretto-17 AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ===== RUN STAGE =====
FROM amazoncorretto:17

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]