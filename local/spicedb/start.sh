#!/bin/bash

log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

error_exit() {
    log "Error: $1"
    exit 1
}

validate_env_var() {
    if [ -z "${!1}" ]; then
        error_exit "Environment variable $1 is not set"
    else
        log "Environment variable $1 is set to ${!1}"
    fi
}

printStep() {
    printf "\033[0;36m\n\n%s\n##########\033[0m  STEP: \e[1;32m%s\033[0;36m  ##########\n%s\033[0;36m\n" "$(printf '#%.0s' $(seq 1 $(( ${#1} + 30 ))))" "$1" "$(printf '#%.0s' $(seq 1 $(( ${#1} + 30 ))))"
}

env_vars=("POSTGRES_PASSWORD" "POSTGRES_USER" "POSTGRES_DB" "POSTGRES_PORT" "POSTGRES_HOST" "SPICEDB_HOST" "SPICEDB_KEY")

for var in "${env_vars[@]}"
do
    validate_env_var "$var"
done

postgres_uri="postgres://$POSTGRES_USER:$POSTGRES_PASSWORD@$POSTGRES_HOST:$POSTGRES_PORT/$POSTGRES_DB?sslmode=disable"

printStep "Migrating Postgres datastore to SpiceDB"

spicedb migrate head --datastore-engine postgres --datastore-conn-uri "$postgres_uri" || error_exit "Failed to migrate spicedb"

printStep "Starting SpiceDB"

spicedb serve --grpc-preshared-key "$SPICEDB_KEY" --datastore-engine=postgres --datastore-conn-uri="$postgres_uri" &

serve_pid=$!

# Wait for spicedb serve to start
sleep 5

./initialize_schema.sh

# put spicedb logs to foreground
wait $serve_pid || error_exit "Failed to start SpiceDB"