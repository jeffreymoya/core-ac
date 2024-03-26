package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.CheckRoutePermissionDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Aspect
@Component
public class ControllerAuditAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerAuditAspect.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${p8c.security.principal-header}")
    private String userId;

    @Value("${p8c.route-check.constants.subjectType}")
    private String subjectType;

    @Value("${p8c.security.client-ip-header}")
    private String clientIpHeader;

    @Around("execution(* com.synpulse8.pulse8.core.accesscontrolsvc.controller.AttributeController.*(..))")
    private CompletableFuture<?> aroundAttributeControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return (CompletableFuture<?>) aroundControllerMethod(P8CKafkaTopic.LOGS_ATTRIBUTES, joinPoint);
    }

    @Around("execution(* com.synpulse8.pulse8.core.accesscontrolsvc.controller.RoleController.*(..))")
    private CompletableFuture<?> aroundRoleControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return (CompletableFuture<?>) aroundControllerMethod(P8CKafkaTopic.LOGS_ROLES, joinPoint);
    }

    @Around("execution(* com.synpulse8.pulse8.core.accesscontrolsvc.controller.PermissionsController.*(..))")
    private CompletableFuture<?> aroundPermissionsControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return (CompletableFuture<?>) aroundControllerMethod(P8CKafkaTopic.LOGS_PERMISSIONS, joinPoint);
    }

    @Around("execution(* com.synpulse8.pulse8.core.accesscontrolsvc.controller.PolicyController.*(..))")
    private CompletableFuture<?> aroundPolicyControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return (CompletableFuture<?>) aroundControllerMethod(P8CKafkaTopic.LOGS_POLICIES, joinPoint);
    }

    @Around("execution(* com.synpulse8.pulse8.core.accesscontrolsvc.controller.SchemaController.*(..))")
    private CompletableFuture<?> aroundSchemaControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return (CompletableFuture<?>) aroundControllerMethod(P8CKafkaTopic.LOGS_SCHEMAS, joinPoint);
    }

    @Around("execution(* com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CExceptionHandler.handleValidationExceptions(..))")
    private ResponseEntity<?> logException(ProceedingJoinPoint joinPoint) throws Throwable {
        return (ResponseEntity<?>) aroundControllerMethod(null, joinPoint);
    }

    @Around("execution(* com.synpulse8.pulse8.core.accesscontrolsvc.controller.RelationshipController.*(..))")
    private CompletableFuture<?> aroundRelationshipControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return aroundControllerMethodRelationship(P8CKafkaTopic.LOGS_RELATIONSHIPS, joinPoint);
    }

    private CompletableFuture<?> aroundControllerMethodRelationship(String topic, ProceedingJoinPoint joinPoint) throws Throwable {
        AuditLog.AuditLogBuilder builder = captureRequestDetails(topic, joinPoint);
        try {
            CompletableFuture<?> resultFuture = (CompletableFuture<?>) joinPoint.proceed();
            processResponseDetails(resultFuture, builder);
            return resultFuture;
        } catch (Throwable throwable) {
            builder.errorMessage(throwable.getMessage());
            logAsync(builder.build());
            throw throwable;
        }
    }

    private Object aroundControllerMethod(String topic, ProceedingJoinPoint joinPoint) throws Throwable {
        AuditLog.AuditLogBuilder builder = captureRequestDetails(topic, joinPoint);
        try {
            Object result = joinPoint.proceed();
            processResponseDetails(result, builder);
            return result;
        } catch (Throwable throwable) {
            builder.errorMessage(getErrorMessage(throwable));
            logAsync(builder.build());
            throw throwable;
        }
    }

    @Async
    public void processResponseDetails(Object result, AuditLog.AuditLogBuilder builder) throws JsonProcessingException {
        if (result == null) logAsync(builder.build());
        else if (result instanceof CompletableFuture<?>) {
            ((CompletableFuture<?>) result).whenCompleteAsync((response, throwable) -> {
                try {
                    if (throwable != null) builder.errorMessage(getErrorMessage(throwable));
                    else builder.response(response instanceof String ? (String) response : objectMapper.writeValueAsString(response));
                    logAsync(builder.build());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        else if (result instanceof ResponseEntity<?>) {
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            String message = objectMapper.writeValueAsString(response);
            if (response.getStatusCode().isError()) builder.errorMessage(message);
            else builder.response(message);
            logAsync(builder.build());
        }
    }

    private AuditLog.AuditLogBuilder captureRequestDetails(String topic, ProceedingJoinPoint joinPoint) throws JsonProcessingException {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String methodName = request.getMethod();
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        if (topic == null) topic = getTopicFromPath(path);
        String stepName = joinPoint.getSignature().getName();

        List<Object> args = new ArrayList<>();
        args.addAll(Arrays.asList(joinPoint.getArgs()));
        if (topic.equals(P8CKafkaTopic.LOGS_PERMISSIONS) & stepName.equals("routeCheck")) {
            args.removeIf(obj -> !(obj instanceof CheckRoutePermissionDto));
            args.add(getRequestHeaders(request));
        }
        else if (stepName.equals("handleValidationExceptions")) {
            args = args.stream().filter(obj -> obj instanceof MethodArgumentNotValidException)
                    .map(ex -> ((MethodArgumentNotValidException) ex).getBindingResult().getTarget()).toList();
        }
        String user = request.getHeader(userId);

        AuditLog.AuditLogBuilder auditLogBuilder = AuditLog.builder()
                .timestamp(Instant.now().toString())
                .userId(user)
                .methodName(methodName)
                .topic(topic)
                .stepName(stepName)
                .path(path)
                .queryString(queryString);

        if (args != null && args.size() > 0) auditLogBuilder.requestArgs(objectMapper.writeValueAsString(args));

        return auditLogBuilder;
    }

    private String getErrorMessage(Throwable throwable) {
        String cause = Optional.ofNullable(throwable.getCause()).flatMap(x -> Optional.ofNullable(x.getCause())
                .map(y -> "; " + y)).orElse("");
        return throwable.getMessage() == null? throwable.toString() : throwable.getMessage() + cause;
    }

    private void logAsync(AuditLog auditLog) throws JsonProcessingException {
        String logMessage = objectMapper.writeValueAsString(auditLog);
        Marker topicMarker = MarkerFactory.getMarker(auditLog.getTopic());
        LOGGER.info(topicMarker, logMessage);
    }

    private String getTopicFromPath(String path) {
        String resource = Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).skip(1).findFirst().orElse("");
        return switch (resource) {
            case "attributes" -> P8CKafkaTopic.LOGS_ATTRIBUTES;
            case "policies", "policy" -> P8CKafkaTopic.LOGS_POLICIES;
            case "permissions" -> P8CKafkaTopic.LOGS_PERMISSIONS;
            case "schema" -> P8CKafkaTopic.LOGS_SCHEMAS;
            case "roles" -> P8CKafkaTopic.LOGS_ROLES;
            case "relationships" -> P8CKafkaTopic.LOGS_RELATIONSHIPS;
            default -> "";
        };
    }

    private Map getRequestHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .filter(headerName -> headerName.equalsIgnoreCase(subjectType) || headerName.equalsIgnoreCase(clientIpHeader))
                .collect(Collectors.toMap(name -> name, request::getHeader));
    }

}
