package com.synpulse8.pulse8.core.accesscontrolsvc.exception;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsServiceImpl;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class SpiceDBExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpiceDBExceptionHandler.class);

    @ExceptionHandler({StatusRuntimeException.class})
    public ResponseEntity<Object> statusRuntimeException(StatusRuntimeException ex, HttpServletRequest request) throws IOException {
        List<String> errorMessages = Arrays.asList(ex.getMessage().split(":"));
        String errorMessage = errorMessages.get(0);

        return new ResponseEntity<>(Collections.singletonMap("error", errorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler({InvalidDefinitionException.class})
    public ResponseEntity<Object> invalidDefinitionException(InvalidDefinitionException ex, HttpServletRequest request) throws IOException {
        return new ResponseEntity<>(Collections.singletonMap("error", ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}