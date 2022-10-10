package com.mfs.client.equity.exception;

import javax.validation.ConstraintViolationException;

import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ws.client.WebServiceIOException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * Class will handle the various Exceptions that may be experienced in application.
 */
@AllArgsConstructor
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@Log4j2
public class GlobalExceptionHandler {

    final MessageSource messageSource;

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataAccessException.class)
    public @ResponseBody
    ResponseEntity<?> dbError(DataAccessException exception) {
        log.info("Database error: {}", exception.getMessage());
        log.error("Application failure occurred due to database operation!", exception);
        MFSErrorResponse errorResponse = MFSErrorResponse.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .message(exception.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public @ResponseBody
    ResponseEntity<?> dataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.info("Database error: {}", exception.getMessage());
        log.error("Application failure occurred due to database operation!", exception);

        MFSErrorResponse errorResponse = MFSErrorResponse.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @ExceptionHandler(WebServiceIOException.class)
    public @ResponseBody
    ResponseEntity<?> webserviceError(WebServiceIOException exception) {
        log.info("Application error: {}", exception.getMessage());
        log.error("Application failure occurred!", exception);
        
        MFSErrorResponse errorResponse;
        if (exception.getMessage().contains("ConnectTimeoutException")) {
            errorResponse = MFSErrorResponse.builder()
                    .statusCode(HttpStatus.REQUEST_TIMEOUT.value())
                    .error(String.valueOf(HttpStatus.REQUEST_TIMEOUT.value()))
                    .message("Processing host cannot be reached")
                    .build();
        } else {
            errorResponse = MFSErrorResponse.builder()
                    .statusCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                    .error(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()))
                    .message(exception.getMessage())
                    .build();
        }
        return new ResponseEntity<>(errorResponse, HttpStatus.REQUEST_TIMEOUT);

    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MissingConfigValueException.class)
    public @ResponseBody
    ResponseEntity<?> systemConfigError(MissingConfigValueException exception) {
        log.info("Missing system config error: {}", exception.getMessage());
        log.error("Missing config value error", exception);
        

        MFSErrorResponse errorResponse = MFSErrorResponse.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .message(exception.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public @ResponseBody
    ResponseEntity<?> applicationFailure(RuntimeException exception) {
        log.info("Application failure error: {}", exception.getMessage());
        log.error("Application failure occurred!", exception);
        

        MFSErrorResponse errorResponse = MFSErrorResponse.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(exception.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(ConstraintViolationException.class)
    public @ResponseBody
    ResponseEntity<?> handleValidationException(ConstraintViolationException exception) {
        log.info("Database error: {}", exception.getMessage());
        log.error("ConstraintViolationException", exception);
        
        MFSErrorResponse errorResponse;
        if (exception.getConstraintViolations() == null) {
            errorResponse = MFSErrorResponse.builder()
                    .statusCode(HttpStatus.NOT_ACCEPTABLE.value())
                    .error(String.valueOf(HttpStatus.NOT_ACCEPTABLE.value()))
                    .message(exception.getMessage())
                    .build();
        } else {
            StringBuilder errorMsgBuilder = new StringBuilder();
            exception.getConstraintViolations().stream().forEach(cv -> errorMsgBuilder.append(cv.getMessage()));
            errorResponse = MFSErrorResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .error(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                    .message(errorMsgBuilder.toString())
                    .build();
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateRequestException.class)
    public @ResponseBody
    ResponseEntity<?> duplicateRequestException(DuplicateRequestException exception) {
        log.info("Duplication transaction error: {}", exception.getMessage());
        log.error("DuplicateRequestException occurred!", exception);
        

        MFSErrorResponse errorResponse = MFSErrorResponse.builder()
                .statusCode(HttpStatus.CONFLICT.value())
                .error(String.valueOf(HttpStatus.CONFLICT.value()))
                .message(exception.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchTransactionExistsException.class)
    @ResponseBody
    public MFSErrorResponse notFoundException(NoSuchTransactionExistsException exception) {
        log.info("No such transaction error: {}", exception.getMessage());
        log.error("NoSuchTransactionExistsException", exception);
        

        MFSErrorResponse errorResponse = MFSErrorResponse.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .error(String.valueOf(HttpStatus.NOT_FOUND.value()))
                .message("Transaction reference given does not exist")
                .build();

        return errorResponse;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(EquityClientException.class)
    @ResponseBody
    public MFSErrorResponse equityClientException(EquityClientException exception) {
        log.error("EquityClientException", exception);
        

        MFSErrorResponse errorResponse = MFSErrorResponse.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .message(exception.getMessage())
                .build();

        return errorResponse;
    }

    @Data
    @Builder
    public static class MFSErrorResponse {

        int statusCode;
        String error;
        String message;
    }

}
