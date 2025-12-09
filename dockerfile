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
CMD ["java", "-jar", "target/LiftAKids-0.0.1-SNAPSHOT.jar"]