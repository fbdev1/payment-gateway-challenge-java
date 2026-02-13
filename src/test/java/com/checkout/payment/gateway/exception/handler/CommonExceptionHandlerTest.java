package com.checkout.payment.gateway.exception.handler;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.AcquiringProcessException;
import com.checkout.payment.gateway.exception.EntityNotFoundException;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.ErrorResponse;
import com.checkout.payment.gateway.model.ErrorResponseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommonExceptionHandlerTest {

  private CommonExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    exceptionHandler = new CommonExceptionHandler();
  }

  @Test
  void handleNotFoundException_ReturnsNotFoundResponse() {
    EntityNotFoundException exception = new EntityNotFoundException("Entity not found");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFoundException(exception);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Page not found", response.getBody().getMessage());
  }

  @Test
  void handleAcquiringBankClientException_ReturnsBadGatewayResponse() {
    AcquiringProcessException exception = new AcquiringProcessException("Bank processing error");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleAcquiringBankClientException(exception);

    assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Error processing payment. Acquiring Bank integration error.Try again later.", response.getBody().getMessage());
  }

  @Test
  void handleValidationException_WithFieldErrors_ReturnsBadRequestWithFieldErrors() {
    MethodArgumentNotValidException exception = createValidationExceptionWithFieldErrors();

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertInstanceOf(ErrorResponseStatus.class, response.getBody());

    ErrorResponseStatus errorResponse = (ErrorResponseStatus) response.getBody();
    assertEquals(PaymentStatus.REJECTED.getName(), errorResponse.getStatus());
    assertTrue(errorResponse.getMessage().contains("field1: error message 1"));
    assertTrue(errorResponse.getMessage().contains("field2: error message 2"));
  }

  @Test
  void handleValidationException_WithObjectErrors_ReturnsBadRequestWithObjectErrors() {
    MethodArgumentNotValidException exception = createValidationExceptionWithObjectErrors();

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertInstanceOf(ErrorResponseStatus.class, response.getBody());

    ErrorResponseStatus errorResponse = (ErrorResponseStatus) response.getBody();
    assertEquals(PaymentStatus.REJECTED.getName(), errorResponse.getStatus());
    assertTrue(errorResponse.getMessage().contains("object error 1"));
    assertTrue(errorResponse.getMessage().contains("object error 2"));
  }

  @Test
  void handleValidationException_WithNoErrors_ReturnsBadRequestWithEmptyMessage() {
    MethodArgumentNotValidException exception = createValidationExceptionWithNoErrors();

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertInstanceOf(ErrorResponseStatus.class, response.getBody());

    ErrorResponseStatus errorResponse = (ErrorResponseStatus) response.getBody();
    assertEquals(PaymentStatus.REJECTED.getName(), errorResponse.getStatus());
    assertEquals("", errorResponse.getMessage());
  }

  @Test
  void handleMethodArgumentException_ReturnsBadRequestResponse() {
    MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
        "invalid", Integer.class, "paramName", null, new IllegalArgumentException("Invalid format")
    );

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Invalid argument format", response.getBody().getMessage());
  }

  @Test
  void handleException_EventProcessingException_ReturnsNotFoundResponse() {
    EventProcessingException exception = new EventProcessingException("Event processing failed");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(exception);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Page not found", response.getBody().getMessage());
  }

  @Test
  void handleJsonParseException_ReturnsNotFoundResponse() {
    HttpMessageNotReadableException exception = new HttpMessageNotReadableException("JSON parse error");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleJsonParseException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("JSON parse error", response.getBody().getMessage());
  }

  @Test
  void handleGenericException_ReturnsInternalServerErrorResponse() {
    Exception exception = new RuntimeException("Unexpected error");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Unexpected error", response.getBody().getMessage());
  }

  @Test
  void handleGenericException_WithNullMessage_ReturnsInternalServerErrorResponse() {
    Exception exception = new RuntimeException((String) null);

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNull(response.getBody().getMessage());
  }

  private MethodArgumentNotValidException createValidationExceptionWithFieldErrors() {
    MethodParameter methodParameter = mock(MethodParameter.class);
    BindingResult bindingResult = mock(BindingResult.class);
    List<FieldError> fieldErrors = new ArrayList<>();
    fieldErrors.add(new FieldError("object", "field1", "rejected value", false, null, null, "error message 1"));
    fieldErrors.add(new FieldError("object", "field2", "rejected value", false, null, null, "error message 2"));

    when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

    return new MethodArgumentNotValidException(methodParameter, bindingResult);
  }

  private MethodArgumentNotValidException createValidationExceptionWithObjectErrors() {
    MethodParameter methodParameter = mock(MethodParameter.class);
    BindingResult bindingResult = mock(BindingResult.class);
    List<ObjectError> objectErrors = new ArrayList<>();
    objectErrors.add(new ObjectError("object", "object error 1"));
    objectErrors.add(new ObjectError("object", "object error 2"));

    when(bindingResult.getFieldErrors()).thenReturn(new ArrayList<>());
    when(bindingResult.getAllErrors()).thenReturn(objectErrors);

    return new MethodArgumentNotValidException(methodParameter, bindingResult);
  }

  private MethodArgumentNotValidException createValidationExceptionWithNoErrors() {
    MethodParameter methodParameter = mock(MethodParameter.class);
    BindingResult bindingResult = mock(BindingResult.class);
    when(bindingResult.getFieldErrors()).thenReturn(new ArrayList<>());
    when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>());

    return new MethodArgumentNotValidException(methodParameter, bindingResult);
  }
}