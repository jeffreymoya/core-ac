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

    private String relationshipsAuditLog;

    private String permissionsAuditLog;

    private String policiesAuditLog;

    private String schemasAuditLog;

    private final CountDownLatch attributeLatch = new CountDownLatch(1);

    private final CountDownLatch roleLatch = new CountDownLatch(1);

    private final CountDownLatch relationshipLatch = new CountDownLatch(1);

    private final CountDownLatch permissionLatch = new CountDownLatch(1);

    private final CountDownLatch policyLatch = new CountDownLatch(1);

    private final CountDownLatch schemaLatch = new CountDownLatch(1);

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

    @KafkaListener(topics = P8CKafkaTopic.LOGS_RELATIONSHIPS, groupId = "ac.audit.role", containerFactory = "kafkaListenerLog4jContainerFactory")
    public void relationshipListener(String record) {
        relationshipsAuditLog = record;
        relationshipLatch.countDown();
    }

    @KafkaListener(topics = P8CKafkaTopic.LOGS_PERMISSIONS, groupId = "ac.audit.role", containerFactory = "kafkaListenerLog4jContainerFactory")
    public void permissionListener(String record) {
        permissionsAuditLog = record;
        permissionLatch.countDown();
    }

    @KafkaListener(topics = P8CKafkaTopic.LOGS_POLICIES, groupId = "ac.audit.role", containerFactory = "kafkaListenerLog4jContainerFactory")
    public void policyListener(String record) {
        policiesAuditLog = record;
        policyLatch.countDown();
    }

    @KafkaListener(topics = P8CKafkaTopic.LOGS_SCHEMAS, groupId = "ac.audit.role", containerFactory = "kafkaListenerLog4jContainerFactory")
    public void schemaListener(String record) {
        schemasAuditLog = record;
        schemaLatch.countDown();
    }

}
