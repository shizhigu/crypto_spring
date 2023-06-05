#
# Build stage
#
FROM maven:3.9.2-amazoncorretto-17 AS build
COPY . .
RUN mvn clean package install

#
# Package stage
#
FROM openjdk:17
VOLUME /tmp
EXPOSE 8888
ARG JAR_FILE=target/*.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]