# Use Alpine for smaller size
FROM openjdk:17-alpine

# Install Maven
RUN apk add --no-cache maven

WORKDIR /app

# Copy source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Run the application
CMD ["java", "-jar", "target/LiftAKids-0.0.1-SNAPSHOT.jar app.jar"]







# Build stage
#FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
#WORKDIR /app
#COPY pom.xml .
#COPY src ./src
#RUN mvn clean package -DskipTests
#
#
## Run stage
#FROM eclipse-temurin:17-jre-alpine
#WORKDIR /app
#COPY --from=build /app/target/LiftAKids-0.0.1-SNAPSHOT.jar app.jar
#
#CMD ["java", "-jar", "app.jar"]
