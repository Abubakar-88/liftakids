

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

#CMD ["java", "-jar", "app.jar"]
# Explicitly set the port
ENV SERVER_PORT=8000
ENV SERVER_ADDRESS=0.0.0.0

# Expose the port
EXPOSE 8000

# Run with verbose output
CMD ["java", \
     "-Dserver.port=8000", \
     "-Dserver.address=0.0.0.0", \
     "-Djava.net.preferIPv4Stack=true", \
     "-verbose:class", \
     "-jar", "app.jar"]