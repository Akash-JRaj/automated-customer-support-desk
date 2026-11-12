# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy Maven wrapper & POM files
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Resolve dependencies for Docker cache optimization
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and package application
COPY src src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root application user for container security
RUN addgroup -S supportdesk && adduser -S supportdesk -G supportdesk
USER supportdesk:supportdesk

# Copy packaged jar from builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Expose microservice and management ports
EXPOSE 8080

# Configure JVM memory settings & healthcheck
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
