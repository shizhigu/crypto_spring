#
# Build stage
#
FROM maven:3.9.2-amazoncorretto-17 AS build
WORKDIR /home/app
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn clean package install

#
# Package stage
#
FROM openjdk:17
VOLUME /tmp
EXPOSE 8888
COPY --from=build /home/app/target/*.jar /home/app.jar
ENTRYPOINT ["java","-jar","/home/app.jar"]