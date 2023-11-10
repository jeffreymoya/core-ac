package com.synpulse8.pulse8.core.yournamebackend.kafka.consumer;

import lombok.extern.log4j.Log4j2;
import com.synpulse8.pulse8.core.ProvisionTenant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "CREATE_TRIGGER_TENANT_PROVISIONING", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, ProvisionTenant> record) {
        log.debug("Received message: " +  record.value());
    }

}
