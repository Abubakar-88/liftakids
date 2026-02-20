# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests


# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/LiftAKids-0.0.1-SNAPSHOT.jar app.jar

# Expose the port
EXPOSE 8000

# Bind to all interfaces
CMD ["java", \
     "-Dserver.address=0.0.0.0", \
     "-Dserver.port=8000", \
     "-jar", \
     "app.jar"]
