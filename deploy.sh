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
