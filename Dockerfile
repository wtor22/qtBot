FROM openjdk:17-jdk-slim
LABEL authors="Wtor22"
WORKDIR /app
COPY target/tg-bot-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/application*.yml /app/
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]