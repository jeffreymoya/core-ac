#!/bin/sh

create_topic() {
    topicList=(
        create-relationship
        delete-relationship
        update-relationship
        delete-specific-relationship
        logs-attributes
        logs-relationships
        logs-roles
        logs-permissions
        logs-policies
        logs-schemas

     )
        echo -e "zookeeper:2182 partition 1 replication-factor 1"
        for topicName in ${topicList[*]}; do
          echo -e "Creating topic '$topicName' "
          docker exec pulse8-kafka-ac kafka-topics --bootstrap-server localhost:9092 \
                --create --topic $topicName --partitions 1 --replication-factor 1 --if-not-exists
        done

}

echo -e "Creating topics...\n"

create_topic

# EOF content
echo "\n"

