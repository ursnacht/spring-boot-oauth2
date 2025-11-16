FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent pom first
COPY pom.xml ../pom.xml
COPY backend-service/pom.xml ./pom.xml

# Copy source code
COPY backend-service/src ./src

# Build the application
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose application port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
