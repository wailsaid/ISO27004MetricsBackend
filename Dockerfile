# ── Stage 1: build JAR with Maven ─────────────────────────────────────────────
FROM maven:3.9-amazoncorretto-17 AS builder
WORKDIR /app
COPY pom.xml .
# Download dependencies separately so this layer is cached unless pom.xml changes
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: minimal JRE runtime ──────────────────────────────────────────────
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
