# Multi-stage Docker build for Spring Boot application
# Uses Java 24 with Gradle build system

# Stage 1: Build the application
FROM gradle:8.10-jdk AS build

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files first for better caching
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Download dependencies (this layer will be cached unless build files change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew bootJar --no-daemon

# Stage 2: Create the runtime image
FROM openjdk:24-jdk-slim

# Set maintainer info
LABEL maintainer="Ticket Booking System"
LABEL description="Spring Boot ticket booking application with PayPal integration"

# Create application user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Create directory for logs and change ownership
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8888

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8888/actuator/health || exit 1

# Set JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]