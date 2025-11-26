# Start with a Java 21 (JDK) image
FROM eclipse-temurin:21-jre-alpine

# Set the folder inside the container
WORKDIR /app

# Copy the JAR file from your build folder into the container
# Note: This path works because we will build it in the pipeline
COPY target/*.jar app.jar

# Run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]