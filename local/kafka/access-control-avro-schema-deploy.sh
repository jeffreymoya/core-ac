#!/bin/sh

# Check for jq availability.
if ! [ -x "$(command -v jq)" ]; then
    echo '\nError: jq is not installed.\nhttps://stedolan.github.io/jq/download/\n' >&2
    exit 1
fi

cd $1 || exit

deploy_schema_list() {

        echo "==================== schemas ===================="

        fileList=$(ls *.avsc)
        for file in $fileList; do

            fileContent=$(cat $file)

            # Get topic name from aliases attribute in schema.
            s=$(echo $fileContent | jq '.doc | split("|")[0]')
            echo $s
            s1=${s%\"}
            topicName=${s1#\"}

            url="http://localhost:8081/subjects/${topicName}-value/versions"

            # Writing to dump file to prepare the payload.
            echo $(echo $fileContent | jq '{"schema":.|tostring}') >dump.txt

            echo "Processing: $file\nUrl: $url"
            curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
                --data-binary "@dump.txt" $url
            echo "\n"

            rm dump.txt
        done


}

# ============================ Main ==============================

echo "\nWaiting for schema registry to be online...\n"
response=0
while [ $response -ne 200 ]; do
    response=$(curl --write-out %{http_code} --silent --output /dev/null http://localhost:8081)
    if [ $response -eq 200 ]; then
        deploy_schema_list
    fi
    sleep 1
done