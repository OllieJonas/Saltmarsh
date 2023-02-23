# Build stage
FROM maven:3.9.0-eclipse-temurin-19 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/ /app/src/
RUN mvn package

# Run stage
FROM gcr.io/distroless/java
COPY --from=build /app/target/Saltmarsh.jar /Saltmarsh.jar
CMD ["java", "-jar", "/saltmarsh.jar"]