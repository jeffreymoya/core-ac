package com.synpulse8.pulse8.core.accesscontrolsvc.exception;

import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collections;

@ControllerAdvice
public class SpiceDBExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<Object> statusRuntimeException(StatusRuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(Collections.singletonMap("error", ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}