# Build stage
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app
# Copy everything (respecting .dockerignore)
COPY . .

# Fix potential Windows line ending issues and make gradlew executable
RUN sed -i 's/\r$//' gradlew
RUN chmod +x gradlew

# Build only the server module
RUN ./gradlew :server:installDist --no-daemon

# Run stage
FROM eclipse-temurin:17-jre

ENV PORT 8080
EXPOSE 8080

WORKDIR /app
# Copy the installed distribution from the build stage
COPY --from=build /app/server/build/install/server .

# Ensure the start script is executable
RUN chmod +x bin/server

# Start the server using the generated script
ENTRYPOINT ["./bin/server"]
