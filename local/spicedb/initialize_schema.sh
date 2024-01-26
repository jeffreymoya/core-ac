#!/bin/bash
printStep() {
    printf "\033[0;36m\n\n%s\n##########\033[0m  STEP: \e[1;32m%s\033[0;36m  ##########\n%s\033[0;36m\n" "$(printf '#%.0s' $(seq 1 $(( ${#1} + 30 ))))" "$1" "$(printf '#%.0s' $(seq 1 $(( ${#1} + 30 ))))"
}

export SCHEMA_DIRECTORY=schema_v1.yml
export RELATIONSHIPS_FILE=relationships_v1.json
export SPICEDB_ENDPOINT=localhost:50051
#update value of zed_keyring with your zed keyring password
export ZED_KEYRING=pulse8

printStep "Writing schema for spiceDB"
#Setting context for zed client
ZED_KEYRING_PASSWORD=$ZED_KEYRING zed context set dev $SPICEDB_ENDPOINT $SPICEDB_KEY
#Executing write schema command
ZED_KEYRING_PASSWORD=$ZED_KEYRING zed schema write $SCHEMA_DIRECTORY --endpoint $SPICEDB_ENDPOINT --token $SPICEDB_KEY --insecure
#Reading the schema
ZED_KEYRING_PASSWORD=$ZED_KEYRING zed schema read --insecure

printStep "Writing relationships for spiceDB"

json=$(cat $RELATIONSHIPS_FILE)

echo "$json" | jq -r '.[] | if .optionalSubjectRelation == "" or .optionalSubjectRelation == null then "\(.resource):\(.resourceId) \(.relation) \(.subject):\(.subjectId)" else "\(.resource):\(.resourceId) \(.relation) \(.subject):\(.subjectId)#\(.optionalSubjectRelation)" end' | while read -r line
do
  printf "\e[1;33mrelationship: > zed relationship create %s\e[0m\n" "$line"
  ZED_KEYRING_PASSWORD=$ZED_KEYRING zed relationship create $line --endpoint $SPICEDB_ENDPOINT --token $SPICEDB_KEY --insecure
done