package com.synpulse8.pulse8.core.yournamebackend.kafka.producer;

import lombok.extern.log4j.Log4j2;
import com.synpulse8.pulse8.core.ProvisionTenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, ProvisionTenant> kafkaTemplate;

    public void producer(ProvisionTenant command) {
        kafkaTemplate.send("CREATE_TRIGGER_TENANT_PROVISIONING",
                ProvisionTenant.newBuilder()
                        .setSERIAL(1)
                        .setCORRELATIONID("1")
                        .setTYPE("CREATE")
                        .setUSERACCESSTYPE("BOTH")
                        .build());
    }

}
