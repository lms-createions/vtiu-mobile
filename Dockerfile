# Build stage
FROM gradle:8.10-jdk17 AS build

WORKDIR /app
COPY . .

# Fix line endings and make executable
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Build only the server module using the specific JDK
RUN ./gradlew :server:installDist --no-daemon -Dorg.gradle.java.home=/opt/java/openjdk

# Run stage
FROM eclipse-temurin:17-jre

ENV PORT 8080
EXPOSE 8080

WORKDIR /app
COPY --from=build /app/server/build/install/server .

RUN chmod +x bin/server
ENTRYPOINT ["./bin/server"]
