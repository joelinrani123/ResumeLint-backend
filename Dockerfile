# --- Build stage ---
FROM maven:3.9-eclipse-temurin-22 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:22-jre-jammy
WORKDIR /app
COPY --from=build /build/target/resumelint-backend.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
