FROM openjdk:17-jdk-slim
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean
WORKDIR /app
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]