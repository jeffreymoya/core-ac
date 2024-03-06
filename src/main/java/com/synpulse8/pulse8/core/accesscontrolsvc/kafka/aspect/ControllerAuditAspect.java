package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Aspect
@Component
public class ControllerAuditAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerAuditAspect.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${p8c.security.principal-header}")
    private String userId;

    @Around("execution(* com.synpulse8.pulse8.core.accesscontrolsvc.controller.AttributeController.*(..))")
    private CompletableFuture<?> aroundAttributeControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return aroundControllerMethod(P8CKafkaTopic.LOGS_ATTRIBUTES, joinPoint);
    }

    @Around("execution(* com.synpulse8.pulse8.core.accesscontrolsvc.controller.RoleController.*(..))")
    private CompletableFuture<?> aroundRoleControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return aroundControllerMethod(P8CKafkaTopic.LOGS_ROLES, joinPoint);
    }

    private CompletableFuture<?> aroundControllerMethod(String topic, ProceedingJoinPoint joinPoint) throws Throwable {
        AuditLog.AuditLogBuilder builder = captureRequestDetails(topic, joinPoint);
        try {
            CompletableFuture<?> resultFuture = (CompletableFuture<?>) joinPoint.proceed();
            processResponseDetails(topic, resultFuture, builder);
            return resultFuture;
        } catch (Throwable throwable) {
            builder.errorMessage(throwable.getMessage());
            logAsync(topic, builder.build());
            throw throwable;
        }
    }

    @Async
    public void processResponseDetails(String topic, CompletableFuture<?> resultFuture, AuditLog.AuditLogBuilder builder) throws JsonProcessingException {
        if (resultFuture == null) logAsync(topic, builder.build());
        else {
            resultFuture.whenCompleteAsync((response, throwable) -> {
                try {
                    if (throwable != null) builder.errorMessage(throwable.getMessage());
                    else builder.response(objectMapper.writeValueAsString(response));
                    logAsync(topic, builder.build());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private AuditLog.AuditLogBuilder captureRequestDetails(String topic, ProceedingJoinPoint joinPoint) throws JsonProcessingException {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
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
                .stepName(joinPoint.getSignature().getName())
                .path(path)
                .queryString(queryString);

        if (args != null && args.length > 0) auditLogBuilder.requestArgs(objectMapper.writeValueAsString(args));

        return auditLogBuilder;
    }

    private void logAsync(String topic, AuditLog auditLog) throws JsonProcessingException {
        String logMessage = objectMapper.writeValueAsString(auditLog);
        Marker topicMarker = MarkerFactory.getMarker(topic);
        LOGGER.info(topicMarker, logMessage);
    }

}
