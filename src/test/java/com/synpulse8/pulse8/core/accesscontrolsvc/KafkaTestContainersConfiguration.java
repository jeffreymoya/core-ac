package com.synpulse8.pulse8.core.accesscontrolsvc;

import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.DeleteRelationshipMessage;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaTestContainersConfiguration {

    // KafkaProperties groups all properties prefixed with `spring.kafka`
    private final KafkaProperties props;
    KafkaTestContainersConfiguration(KafkaProperties kafkaProperties) {
        props = kafkaProperties;
    }

    @Bean
    public Map<String, String> consumerConfigs() {
        Map<String, String> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, PermissionsIntegrationTest.kafka.getBootstrapServers());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    /**
     * Mock schema registry bean used by Kafka Avro Serde since
     * the @EmbeddedKafka setup doesn't include a schema registry.
     * @return MockSchemaRegistryClient instance
     */
    @Bean
    MockSchemaRegistryClient schemaRegistryClient() {
        return new MockSchemaRegistryClient();
    }

    /**
     * KafkaAvroSerializer that uses the MockSchemaRegistryClient
     * @return KafkaAvroSerializer instance
     */
    @Bean
    KafkaAvroSerializer kafkaAvroSerializer() {
        return new KafkaAvroSerializer(schemaRegistryClient());
    }

    /**
     * KafkaAvroDeserializer that uses the MockSchemaRegistryClient.
     * The props must be provided so that specific.avro.reader: true
     * is set. Without this, the consumer will receive GenericData records.
     * @return KafkaAvroDeserializer instance
     */
    @Bean
    KafkaAvroDeserializer kafkaAvroDeserializer() {
        return new KafkaAvroDeserializer(schemaRegistryClient(), props.buildConsumerProperties());
    }

    @Bean
    public ProducerFactory<String, DeleteRelationshipMessage> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, PermissionsIntegrationTest.kafka.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    DefaultKafkaConsumerFactory consumerFactory() {
        return new DefaultKafkaConsumerFactory(
                props.buildConsumerProperties(),
                new StringDeserializer(),
                kafkaAvroDeserializer()
        );
    }

    @Bean
    public KafkaTemplate<String, DeleteRelationshipMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
