FROM maven:3-eclipse-temurin-17 AS build

ARG SERVER_USERNAME
ARG SERVER_PASSWORD

COPY src build/src
COPY pom.xml /build
COPY settings.xml /build

RUN mvn -s /build/settings.xml -f /build/pom.xml clean package -P ci -DskipTests

FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install bash && mkdir /app

COPY --from=build /build/target/*.jar /app/pulse8-core-access-control.jar

RUN mkdir /logs
RUN groupadd -r user
RUN useradd -r -g user user --shell /bin/bash
RUN chown -R user:user /app /logs
RUN chmod 755 /logs
USER user

WORKDIR /app
EXPOSE 3010
EXPOSE 5010
ENTRYPOINT ["java", "-jar", "/app/pulse8-core-access-control.jar"]

