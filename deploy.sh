#!/bin/bash

printStep() {
    echo "\n==================================================================="
    echo "STEP: $1"
    echo "==================================================================="
}

printStep "Compile Pulse8 Core Access Control and push image"
sh ./compile.sh

cd local

printStep "Deploy pulse8-core-access-control image"
docker compose up -d
