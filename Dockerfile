# Start from an official JDK image to build the app
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set the working directory
WORKDIR /app

# Copy your project files into the image
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# ========================

# Use a lightweight JDK image for running the app
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application's port
EXPOSE 8000

# Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]
