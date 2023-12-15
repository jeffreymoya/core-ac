package com.synpulse8.pulse8.core.accesscontrolsvc.exception;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class P8CExceptionHandler {
    @ExceptionHandler({StatusRuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> statusRuntimeException(StatusRuntimeException ex) {
        List<String> errorMessages = Arrays.asList(ex.getMessage().split(":", 2));
        String errorMessage = errorMessages.get(1).trim();

        return new ResponseEntity<>(new P8CError(errorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler({InvalidDefinitionException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> invalidDefinitionException(InvalidDefinitionException ex, HttpServletRequest request) throws IOException {
        return new ResponseEntity<>(new P8CError(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String error = ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return new ResponseEntity<>(new P8CError(error), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(P8CException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleP8cExceptions(P8CException ex) {
        return new ResponseEntity<>(new P8CError(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}