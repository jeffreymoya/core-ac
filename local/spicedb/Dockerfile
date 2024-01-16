FROM ubuntu:latest

ARG pwd
ARG user
ARG postgres_db
ARG postgres_port
ARG postgres_host
ARG spicedb_host
ARG spicedb_key

ENV POSTGRES_PASSWORD=$pwd
ENV POSTGRES_USER=$user
ENV POSTGRES_DB=$postgres_db
ENV POSTGRES_PORT=$postgres_port
ENV POSTGRES_HOST=$postgres_host
ENV SPICEDB_HOST=$spicedb_host
ENV SPICEDB_KEY=$spicedb_key

RUN apt update && apt upgrade -y

# jq used for parsing relationships json
RUN apt install -y jq

RUN apt install -y curl ca-certificates gpg && \
    curl https://apt.fury.io/authzed/gpg.key | apt-key add - && \
    echo "deb https://apt.fury.io/authzed/ * *" > /etc/apt/sources.list.d/fury.list && \
    apt update && apt install -y zed

WORKDIR /app

COPY --from=authzed/spicedb:latest /usr/local/bin/spicedb /usr/local/bin/spicedb

ENTRYPOINT ["/app/start.sh"]