package com.checkout.payment.gateway.exception.handler;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.AcquiringProcessException;
import com.checkout.payment.gateway.exception.EntityNotFoundException;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.ErrorResponse;
import com.checkout.payment.gateway.model.ErrorResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundException(EntityNotFoundException ex) {
    return new ResponseEntity<>(new ErrorResponse("Page not found"),
        NOT_FOUND);
  }

  @ExceptionHandler(AcquiringProcessException.class)
  public ResponseEntity<ErrorResponse> handleAcquiringBankClientException(AcquiringProcessException ex) {
    return new ResponseEntity<>(new ErrorResponse("Error processing payment. Acquiring Bank integration error.Try again later."),
        BAD_GATEWAY);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    List<String> errors = new ArrayList<>();
    ex.getBindingResult()
        .getFieldErrors().forEach(
            errorField -> errors.add(errorField.getField() + ": " + errorField.getDefaultMessage()));

    if (errors.isEmpty()) {
      ex.getBindingResult().getAllErrors().stream()
          .map(ObjectError::getDefaultMessage)
          .filter(Objects::nonNull)
          .forEach(errors::add);
    }
    LOG.warn("Failed payment, invalid information was supplied, next validations were broken: {}", errors);
    return new ResponseEntity<>(new ErrorResponseStatus(PaymentStatus.REJECTED.getName(), String.join("\n; ", errors)), BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentException(MethodArgumentTypeMismatchException ex) {
    LOG.error("Invalid argument format", ex);
    return new ResponseEntity<>(new ErrorResponse("Invalid argument format"),
        BAD_REQUEST);
  }

  @ExceptionHandler(EventProcessingException.class)
  public ResponseEntity<ErrorResponse> handleException(EventProcessingException ex) {
    LOG.error("Exception happened", ex);
    return new ResponseEntity<>(new ErrorResponse("Page not found"),
        HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleJsonParseException(HttpMessageNotReadableException ex) {
    LOG.error("Exception happened", ex);
    return new ResponseEntity<>(new ErrorResponse(ex.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleJsonParseException(HttpMediaTypeNotSupportedException ex) {
    LOG.error("Exception happened", ex);
    return new ResponseEntity<>(new ErrorResponse(ex.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    LOG.error("An unexpected error occurred", ex);
    return new ResponseEntity<>(new ErrorResponse(ex.getMessage()),
        INTERNAL_SERVER_ERROR);
  }
}
