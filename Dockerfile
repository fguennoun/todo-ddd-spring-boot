# =================================================================
# Multi-stage Dockerfile for Todo DDD Spring Boot Application
# =================================================================

# Stage 1: Build Stage
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src/

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime Stage
FROM eclipse-temurin:21-jre AS runtime

# Install required packages and create user
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/todo-ddd-spring-boot-*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -Djava.security.egd=file:/dev/./urandom"

# Application configuration
ENV SPRING_PROFILES_ACTIVE=prod

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Metadata
LABEL maintainer="todo-team@example.com"
LABEL version="1.0.0"
LABEL description="Todo DDD Reference Application"
LABEL org.opencontainers.image.source="https://github.com/example/todo-ddd-springboot-app"
