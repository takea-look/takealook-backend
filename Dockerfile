# Stage 1: Build the application
FROM gradle:8.8.0-jdk21 AS build

WORKDIR /home/gradle/src

# Copy settings and build scripts first to leverage Docker layer caching
COPY settings.gradle.kts build.gradle.kts gradlew gradlew.bat ./ 
COPY gradle ./gradle
COPY build-logic ./build-logic

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy the rest of the source code
COPY . .

# Grant execute permission to the Gradle wrapper
RUN chmod +x ./gradlew

# Build the application executable jar
RUN ./gradlew :app:bootJar --no-daemon

# Stage 2: Create the final image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the executable JAR from the build stage
COPY --from=build /home/gradle/src/app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]