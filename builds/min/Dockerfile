FROM maven:3-openjdk-17-slim AS build

ARG SERVER_USERNAME
ARG SERVER_PASSWORD

COPY src build/src
COPY pom.xml /build
COPY settings.xml /build

RUN mvn -s /build/settings.xml -f /build/pom.xml clean install -P ci

FROM openjdk:17-alpine
COPY --from=build /build/target/*-exec.jar pulse8-yourname-backend.jar

ENTRYPOINT ["java","-jar","/pulse8-yourname-backend.jar"]

FROM alpine:3

RUN apk add curl \
    && curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin \
    && trivy rootfs --exit-code 1 --no-progress /

