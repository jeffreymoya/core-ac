package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@Aspect
@Component
public class ControllerAuditAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerAuditAspect.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${p8c.security.principal-header}")
    private String userId;

    @Before("execution(* com.synpulse8.pulse8.core.accesscontrolsvc.controller.AttributeController.*(..))")
    private void beforeAttributeControllerMethod(JoinPoint joinPoint) throws JsonProcessingException {
        beforeControllerMethod(P8CKafkaTopic.LOGS_ATTRIBUTES, joinPoint);
    }

    private void beforeControllerMethod(String topic, JoinPoint joinPoint) throws JsonProcessingException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String methodName = request.getMethod();
        String path = request.getRequestURI();
        String queryString = request.getQueryString();

        Object[] args = joinPoint.getArgs();
        String user = request.getHeader(userId);

        AuditLog.AuditLogBuilder auditLogBuilder = AuditLog.builder()
                .timestamp(Instant.now().toString())
                .userId(user)
                .methodName(methodName)
                .topic(topic)
                .path(path)
                .queryString(queryString);

        if (args != null && args.length > 0) auditLogBuilder.details(objectMapper.writeValueAsString(args));

        AuditLog auditLog = auditLogBuilder.build();

        String logMessage = objectMapper.writeValueAsString(auditLog);

        Marker topicMarker = MarkerFactory.getMarker(topic);

        LOGGER.info(topicMarker, logMessage);

    }

}
