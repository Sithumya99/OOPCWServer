FROM openjdk:19-jdk-slim
WORKDIR /app
RUN mkdir -p /app/OOPCW/logs && chmod -R 777 /app/OOPCW/logs
COPY target/OOP-CW-Server.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]