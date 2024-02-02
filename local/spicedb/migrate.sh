#!/bin/bash

# Function to display usage
usage() {
    echo "Usage: $0 [--schema SCHEMA_FILE [--relationships RELATIONSHIPS_FILE]] --endpoint ENDPOINT --key KEY --keyring KEYRING [--rollback]"
    echo "   --schema, -s          Path to the SpiceDB schema file (required if not using --rollback)"
    echo "   --relationships, -r   Path to the SpiceDB relationships file (optional)"
    echo "   --endpoint, -e        SpiceDB Endpoint"
    echo "   --key, -k             SpiceDB pre-shared key"
    echo "   --keyring, -g         Zed Keyring password"
    echo "   --rollback            Rollback to the latest backup"
    echo "   --help, -h            Display this help message"
    exit 1
}

# Parse command-line arguments
OPTIONS=$(getopt -o s:r:e:k:g:rh --long schema:,relationships:,endpoint:,key:,keyring:,rollback,help -- "$@")
eval set -- "$OPTIONS"

rollback=false

while true; do
  case "$1" in
    -s|--schema) schema_file="$2"; shift 2 ;;
    -r|--relationships) relationships_file="$2"; shift 2 ;;
    -e|--endpoint) endpoint="$2"; shift 2 ;;
    -k|--key) key="$2"; shift 2 ;;
    -g|--keyring) keyring="$2"; shift 2 ;;
    -r|--rollback) rollback=true; shift ;;
    -h|--help) usage ;;
    --) shift; break ;;
    *) echo "Invalid option: -$1. Type --help for usage information" >&2; exit 1 ;;
  esac
done

# Check if schema file exists only if not in rollback mode
if [ "$rollback" = false ] && [ ! -f "$schema_file" ]; then
    echo "Schema file not found. Type --help for usage information"; exit 1;
fi

export ZED_KEYRING_PASSWORD=$keyring

zed context set dev "$endpoint" "$key" --insecure || { echo "Zed Error"; exit 1; }

schema=$(zed schema read 2>&1) || { echo "Unable to connect to SpiceDB server. Exiting.."; exit 1; }

# Create backups directory if it doesn't exist
mkdir -p backups

if [ "$rollback" = true ]; then
    latest_backup=$(ls -t backups/spicedb_* | head -1)
    echo "Rolling back to $latest_backup..."
    zed restore "$latest_backup"
    if [ $? -ne 0 ]; then
        echo "zed rollback failed"
        exit 1
    fi
    rm "$latest_backup"
else
    current_date_time=$(date '+%Y%m%d%H%M%S')

    zed backup backups/spicedb_"$current_date_time"
    if [ $? -ne 0 ]; then
        echo "Zed backup failed. Exiting.."
        exit 1
    fi

    definitions=($(echo "$schema" | grep "^definition " | awk '{print $2}'))

    for definition in "${definitions[@]}"; do
        echo "Deleting relationship for $definition"
        zed relationship bulk-delete "$definition"
         if [ $? -ne 0 ]; then
            echo "Zed relationship delete failed for $definition. Exiting.."
            exit 1
        fi
    done

    zed schema write "$schema_file"
    if [ $? -ne 0 ]; then
        echo "Zed schema write failed. Exiting.."
        exit 1
    fi
    # Process relationships file if it is provided and exists
    if [ -n "$relationships_file" ] && [ -f "$relationships_file" ]; then
        echo "Writing relationships for spiceDB.."
        # Parse the JSON and run the zed command
        cat "$relationships_file" | jq -r '.[] | if .optionalSubjectRelation == "" or .optionalSubjectRelation == null then "\(.resource):\(.resourceId) \(.relation) \(.subject):\(.subjectId)" else "\(.resource):\(.resourceId) \(.relation) \(.subject):\(.subjectId)#\(.optionalSubjectRelation)" end' | while read -r line
        do
          zed relationship create "$line" || { echo "Zed relationship create failed. Exiting.."; exit 1; }
        done
    fi
fi

echo "Operation completed successfully"