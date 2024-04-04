FROM maven:3.8.6-openjdk-18-slim AS build
COPY src /home/app/stubs/src
COPY pom.xml /home/app/stubs
COPY settings.xml /home/app/settings.xml
WORKDIR /home/app/stubs
RUN --mount=type=cache,target=/root/.m2 mvn -s /home/app/settings.xml clean package

FROM amazoncorretto:21
COPY target/LocalStub-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT java -jar app.jar