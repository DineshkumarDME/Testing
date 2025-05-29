package com.jules.project.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuditLogConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditLogConsumer.class);

    // The topic name is injected from application.properties: audit.kafka.topic
    // The group ID is also from application.properties: spring.kafka.consumer.group-id
    @KafkaListener(topics = "${audit.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenToAuditLog(String message) {
        log.info("Received audit log from Kafka: {}", message);

        // TODO: Future implementation will involve parsing this message
        //       and saving the audit details to a dedicated audit database table.
        // Example (pseudo-code):
        // AuditEvent auditEvent = objectMapper.readValue(message, AuditEvent.class);
        // auditRepository.save(auditEvent);
    }
}
