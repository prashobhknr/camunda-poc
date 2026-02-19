# Dockerfile for Camunda 7 POC Application
FROM maven:3.8-openjdk-11 AS build

WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime image
FROM openjdk:11-jre-slim

WORKDIR /app

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy built jar
COPY --from=build /build/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/engine-rest || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
