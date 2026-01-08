FROM maven:3.9.8-eclipse-temurin-21
WORKDIR /app
COPY pom.xml .
COPY src ./src
ENTRYPOINT ["mvn", "-q", "test"]
