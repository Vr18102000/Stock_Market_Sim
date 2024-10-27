# Use a Java 21 base image
FROM eclipse-temurin:21-jdk as builder

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set the working directory
WORKDIR /app

# Copy the project files
COPY pom.xml .
COPY src ./src

# Build the application with Maven
RUN mvn clean package -DskipTests

# Use a minimal Java 21 runtime for the final image
FROM eclipse-temurin:21-jdk

# Set the working directory
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/Stock_Market_Sim-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
