FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn ./.mvn
COPY src ./src
RUN chmod +x mvnw && ./mvnw clean package -DskipTests
EXPOSE 10000
CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]
