FROM docker.io/eclipse-temurin:17.0.7_7-jre

WORKDIR /app
COPY target/ProfileRecognition.jar /app/ProfileRecognition.jar

EXPOSE 8084
CMD ["java", "-jar", "ProfileRecognition.jar"]