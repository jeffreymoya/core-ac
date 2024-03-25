FROM ubuntu:latest

RUN apt update && apt upgrade -y && \
    apt install -y jq iputils-ping curl ca-certificates gpg netcat-openbsd && \
    curl https://apt.fury.io/authzed/gpg.key | apt-key add - && \
    echo "deb https://apt.fury.io/authzed/ * *" > /etc/apt/sources.list.d/fury.list && \
    apt update && apt install -y zed

WORKDIR /app

COPY --from=authzed/spicedb:v1.29.0 /usr/local/bin/spicedb /usr/local/bin/spicedb
COPY start.sh migrate.sh initialize_schema.sh schema_v1.yml relationships_v1.json /app/

RUN groupadd -r user && \
    useradd -r -g user user --shell /bin/bash && \
    chown -R user:user /app && \
    chmod 755 /app

USER user

ENTRYPOINT ["/app/start.sh"]