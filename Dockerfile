FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY src /home/app/stubs/src
COPY pom.xml /home/app/stubs
WORKDIR /home/app/stubs
RUN --mount=type=cache,target=/root/.m2 mvn clean package

FROM amazoncorretto:21
COPY --from=build /home/app/stubs/target/LocalStub-0-SNAPSHOT.jar app.jar
ENTRYPOINT java -jar app.jar