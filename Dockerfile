FROM maven:3.9.0-eclipse-temurin-19 as build
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD . $HOME
RUN mvn package

FROM amazoncorretto:19-alpine-jdk
COPY --from=build /usr/app/target/Saltmarsh-1.0-SNAPSHOT-jar-with-dependencies.jar /app/runner.jar
ENTRYPOINT java -jar /app/runner.jar