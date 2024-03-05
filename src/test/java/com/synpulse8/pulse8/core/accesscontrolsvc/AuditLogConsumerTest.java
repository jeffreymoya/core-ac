package com.synpulse8.pulse8.core.accesscontrolsvc;

import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import lombok.Getter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Getter
@Service
public class AuditLogConsumerTest {

    private String attributesAuditLog;

    private String rolesAuditLog;

    private final CountDownLatch attributeLatch = new CountDownLatch(1);

    private final CountDownLatch roleLatch = new CountDownLatch(1);

    @KafkaListener(topics = P8CKafkaTopic.LOGS_ATTRIBUTES, groupId = "ac.audit.role", containerFactory = "kafkaListenerLog4jContainerFactory")
    public void attributeListener(String record) {
        attributesAuditLog = record;
        attributeLatch.countDown();
    }

    @KafkaListener(topics = P8CKafkaTopic.LOGS_ROLES, groupId = "ac.audit.role", containerFactory = "kafkaListenerLog4jContainerFactory")
    public void roleListener(String record) {
        rolesAuditLog = record;
        roleLatch.countDown();
    }

}
