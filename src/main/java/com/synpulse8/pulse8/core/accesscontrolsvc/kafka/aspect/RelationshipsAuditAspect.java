package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Aspect
@Component
public class RelationshipsAuditAspect {
    public static final String LOG_APP = "APP";
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerAuditAspect.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${p8c.security.principal-header}")
    private String userId;

//    @Before("execution(* com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsServiceImpl.*(..))")
//    public void logBefore(JoinPoint joinPoint) throws JsonProcessingException {
//        long currentTimeMillis = System.currentTimeMillis();
//
//        Marker topicMarker = MarkerFactory.getMarker("logs-relationships");
//
//        LOGGER.info(topicMarker, "type : {}, step-name: {}, timestamp: {}", LOG_APP,
//                joinPoint.getSignature().toString(), currentTimeMillis);
//    }
//
//    @AfterReturning(pointcut = "execution(* com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsServiceImpl.*(..))", returning = "result")
//    public void logAfterReturning(JoinPoint joinPoint, Object result) {
//
//        if (result instanceof CompletableFuture<?>) {
//            CompletableFuture<?> completableFuture = (CompletableFuture<?>) result;
//
//            completableFuture.whenComplete((response, throwable) -> {
//                if (throwable != null) {
//                    // Handle errors if needed
//                    LOGGER.error("Exception occurred in CompletableFuture", throwable);
//                } else {
//                    String methodName = joinPoint.getSignature().getName();
//                    String className = joinPoint.getTarget().getClass().getSimpleName();
//
//                    LOGGER.info("Response from {} in {}: {}", methodName, className, response);
//                }
//            });
//        }
//    }

    @Pointcut("execution(* com.synpulse8.pulse8.core.accesscontrolsvc.controller.RelationshipController.*(..))")
    public void allRelationshipRESTMethods(){};

    @Around("allRelationshipRESTMethods()")
    public Object aroundRelationshipControllerMethod(ProceedingJoinPoint joinPoint) throws JsonProcessingException {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String methodName = request.getMethod();
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String stepName = joinPoint.getSignature().getName();

        Object[] args = joinPoint.getArgs();
        String user = request.getHeader(userId);

        AuditLog.AuditLogBuilder auditLogBuilder = AuditLog.builder()
                .timestamp(Instant.now().toString())
                .userId(user)
                .methodName(methodName)
                .topic(P8CKafkaTopic.LOGS_RELATIONSHIPS)
                .path(path)
                .stepName(stepName)
                .queryString(queryString);

        logAroundRelationshipControllerMethod(args, auditLogBuilder, P8CKafkaTopic.LOGS_RELATIONSHIPS);

        Object returnedValue = null;
        try {
            returnedValue = joinPoint.proceed();

            if (joinPoint.proceed() instanceof CompletableFuture<?>) {
                CompletableFuture<?> completableFuture = (CompletableFuture<?>) joinPoint.proceed();

                completableFuture.whenComplete((response, throwable) -> {
                    if (throwable != null) {
                        // Handle errors if needed
                        LOGGER.error("Exception occurred in CompletableFuture", throwable);
                    } else {
                        auditLogBuilder.response(response.toString());
                        try {
                            logAroundRelationshipControllerMethod(args, auditLogBuilder, P8CKafkaTopic.LOGS_RELATIONSHIPS);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

        } catch (Throwable throwable){
            String failed = "\n*** Method " + methodName
                    + " has failed. " + throwable.getMessage() + " .***";
            LOGGER.error("Error execution due to : " ,failed);
        }

        return returnedValue;
    }

    public void logAroundRelationshipControllerMethod(Object[] args, AuditLog.AuditLogBuilder auditLogBuilder, String topic) throws JsonProcessingException {

        if (args != null && args.length > 0) auditLogBuilder.details(objectMapper.writeValueAsString(args));

        AuditLog auditLog = auditLogBuilder.build();

        String logMessage = objectMapper.writeValueAsString(auditLog);

        Marker topicMarker = MarkerFactory.getMarker(topic);

        LOGGER.info(topicMarker, logMessage);
    }
}
