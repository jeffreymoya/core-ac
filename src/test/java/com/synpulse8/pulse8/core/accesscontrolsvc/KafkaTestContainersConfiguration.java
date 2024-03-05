package com.synpulse8.pulse8.core.accesscontrolsvc;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;

import java.util.Map;

@TestConfiguration
public class KafkaTestContainersConfiguration {

    private final KafkaProperties props;
    KafkaTestContainersConfiguration(KafkaProperties kafkaProperties) {
        props = kafkaProperties;
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
    DefaultKafkaProducerFactory producerFactory() {
        Map<String, Object> configs = props.buildProducerProperties();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, PermissionsIntegrationTest.kafka.getBootstrapServers());
        return new DefaultKafkaProducerFactory<>(configs,
                new StringSerializer(),
                kafkaAvroSerializer());
    }

    @Bean
    DefaultKafkaConsumerFactory consumerFactory() {
        Map<String, Object> configs = props.buildConsumerProperties();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, PermissionsIntegrationTest.kafka.getBootstrapServers());
        return new DefaultKafkaConsumerFactory(configs,
                new StringDeserializer(),
                kafkaAvroDeserializer());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerLog4jContainerFactory() {
        Map<String, Object> configs = props.buildConsumerProperties();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, PermissionsIntegrationTest.kafka.getBootstrapServers());
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "ac.audit.role");
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory(configs,
                new StringDeserializer(),
                new StringDeserializer()));
        return factory;
    }
}
