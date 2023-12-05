#!/bin/bash

printStep() {
    echo "\n==================================================================="
    echo "STEP: $1"
    echo "==================================================================="
}

export SCHEMA_DIRECTORY=schema_v1.yml
export SPICEDB_ENDPOINT=localhost:50051
export SPICEDB_KEY=test_key
#update value of zed_keyring with your zed keyring password
export ZED_KEYRING=test

printStep "Writing schema for spiceDB"
#Setting context for zed client
ZED_KEYRING_PASSWORD=$ZED_KEYRING zed context set dev $SPICEDB_ENDPOINT $SPICEDB_KEY
#Executing write schema command
ZED_KEYRING_PASSWORD=$ZED_KEYRING zed schema write $SCHEMA_DIRECTORY --endpoint $SPICEDB_ENDPOINT --token $SPICEDB_KEY --insecure
#Reading the schema
ZED_KEYRING_PASSWORD=$ZED_KEYRING zed schema read --insecure