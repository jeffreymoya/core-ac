FROM maven:3-openjdk-17-slim AS build

ARG SERVER_USERNAME
ARG SERVER_PASSWORD

COPY src build/src
COPY pom.xml /build
COPY settings.xml /build

RUN mvn -s /build/settings.xml -f /build/pom.xml clean install -P ci -DskipTests

FROM openjdk:17-alpine
COPY --from=build /build/target/*-exec.jar pulse8-core-access-control.jar

ENTRYPOINT ["java","-jar","/pulse8-core-access-control.jar"]

# Enable remote debugging
EXPOSE 5010
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5010", "-jar", "your-application.jar"]

