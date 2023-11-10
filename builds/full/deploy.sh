#!/bin/bash

printStep() {
    echo "\n==================================================================="
    echo "STEP: $1"
    echo "==================================================================="
}

printStep "Compile Pulse8 YourName Backend and push image"
sh ./compile.sh

cd local

printStep "Deploy pulse8-yourname-backend image"
docker compose -p="services" up -d
