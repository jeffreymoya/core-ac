#!/bin/bash

printStep() {
    echo "\n==================================================================="
    echo "STEP: $1"
    echo "==================================================================="
}

printStep "Compile Pulse8 Core Access Control and push image"
sh ./compile.sh

printStep "Deploy pulse8-core-access-control image".
docker compose -f local/docker-compose.yml up -d --build

# Create topics for schemas.
printStep "Create topics"
bash ./local/kafka/pulse8-access-control-topics.sh

# Deploy avro schemas.
printStep "Deploy avro schemas"
sh ./local/kafka/access-control-avro-schema-deploy.sh ./local/kafka/avro/
