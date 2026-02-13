package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.client.model.BankPaymentRequest;
import com.checkout.payment.gateway.client.model.BankPaymentResponse;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.AcquiringProcessException;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankSimulatorDefaultImplTest {

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private BankSimulatorDefaultImpl bankSimulator;

  @Captor
  private ArgumentCaptor<BankPaymentRequest> bankPaymentRequestCaptor;

  private static final String BASE_URL = "http://localhost:8080";
  private UUID paymentId;
  private PostPaymentRequest postPaymentRequest;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(bankSimulator, "baseUrl", BASE_URL);
    paymentId = UUID.randomUUID();
    postPaymentRequest = createPostPaymentRequest("4111111111111111", 12, 2025, "123", "USD",
        10000);
  }

  private PostPaymentRequest createPostPaymentRequest(String cardNumber, int expiryMonth,
      int expiryYear, String cvv,
      String currency, int amount) {
    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber(cardNumber);
    request.setExpiryMonth(expiryMonth);
    request.setExpiryYear(expiryYear);
    request.setCvv(cvv);
    request.setCurrency(currency);
    request.setAmount(amount);
    return request;
  }

  @Test
  void whenMakePayment_andBankAuthorizes_thenReturnAuthorized() {
    BankPaymentResponse bankResponse = new BankPaymentResponse(true, "AUTH123");
    ResponseEntity<BankPaymentResponse> responseEntity = new ResponseEntity<>(bankResponse,
        HttpStatus.OK);

    when(restTemplate.postForEntity(
        eq(BASE_URL + "/payments"),
        any(BankPaymentRequest.class),
        eq(BankPaymentResponse.class)
    )).thenReturn(responseEntity);

    PaymentStatus result = bankSimulator.makePayment(paymentId, postPaymentRequest);

    assertEquals(PaymentStatus.AUTHORIZED, result);
    verify(restTemplate).postForEntity(
        eq(BASE_URL + "/payments"),
        bankPaymentRequestCaptor.capture(),
        eq(BankPaymentResponse.class)
    );

    BankPaymentRequest capturedRequest = bankPaymentRequestCaptor.getValue();
    assertEquals("4111111111111111", capturedRequest.cardNumber());
    assertEquals("12/2025", capturedRequest.expiryDate());
    assertEquals("123", capturedRequest.cvv());
    assertEquals("USD", capturedRequest.currency());
    assertEquals(10000, capturedRequest.amount());
  }

  @Test
  void whenMakePayment_andBankDeclines_thenReturnDeclined() {
    BankPaymentResponse bankResponse = new BankPaymentResponse(false, "UNAUTHORIZED");
    ResponseEntity<BankPaymentResponse> responseEntity = new ResponseEntity<>(bankResponse,
        HttpStatus.OK);

    when(restTemplate.postForEntity(
        eq(BASE_URL + "/payments"),
        any(BankPaymentRequest.class),
        eq(BankPaymentResponse.class)
    )).thenReturn(responseEntity);

    PaymentStatus result = bankSimulator.makePayment(paymentId, postPaymentRequest);

    assertEquals(PaymentStatus.DECLINED, result);
    verify(restTemplate).postForEntity(
        eq(BASE_URL + "/payments"),
        any(BankPaymentRequest.class),
        eq(BankPaymentResponse.class)
    );
  }

  @Test
  void whenMakePayment_andBankReturnsNullBody_thenThrowException() {
    ResponseEntity<BankPaymentResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

    when(restTemplate.postForEntity(
        eq(BASE_URL + "/payments"),
        any(BankPaymentRequest.class),
        eq(BankPaymentResponse.class)
    )).thenReturn(responseEntity);

    AcquiringProcessException exception = assertThrows(
        AcquiringProcessException.class,
        () -> bankSimulator.makePayment(paymentId, postPaymentRequest)
    );

    assertEquals("Bank returned null body for payment ID: " + paymentId,
        exception.getMessage());
  }

  @Test
  void whenMakePayment_andBankAuthorizesWithoutCode_thenThrowException() {
    BankPaymentResponse bankResponse = new BankPaymentResponse(true, null);
    ResponseEntity<BankPaymentResponse> responseEntity = new ResponseEntity<>(bankResponse,
        HttpStatus.OK);

    when(restTemplate.postForEntity(
        eq(BASE_URL + "/payments"),
        any(BankPaymentRequest.class),
        eq(BankPaymentResponse.class)
    )).thenReturn(responseEntity);

    AcquiringProcessException exception = assertThrows(
        AcquiringProcessException.class,
        () -> bankSimulator.makePayment(paymentId, postPaymentRequest)
    );

    assertEquals("Bank returned empty authorization code for payment ID: " + paymentId,
        exception.getMessage());
  }

  @Test
  void whenMakePayment_andRestTemplateThrowsException_thenThrowAcquiringProcessException() {
    when(restTemplate.postForEntity(
        eq(BASE_URL + "/payments"),
        any(BankPaymentRequest.class),
        eq(BankPaymentResponse.class)
    )).thenThrow(new RestClientException("Network error"));

    AcquiringProcessException exception = assertThrows(
        AcquiringProcessException.class,
        () -> bankSimulator.makePayment(paymentId, postPaymentRequest)
    );

    assertEquals("Bank simulator respond with error while making payment request for payment ID: "
        + paymentId, exception.getMessage());
  }

  @Test
  void whenMakePayment_andUnexpectedExceptionOccurs_thenThrowAcquiringProcessException() {
    when(restTemplate.postForEntity(
        eq(BASE_URL + "/payments"),
        any(BankPaymentRequest.class),
        eq(BankPaymentResponse.class)
    )).thenThrow(new RuntimeException("Unexpected error"));

    AcquiringProcessException exception = assertThrows(
        AcquiringProcessException.class,
        () -> bankSimulator.makePayment(paymentId, postPaymentRequest)
    );

    assertEquals("Bank simulator respond with error while making payment request for payment ID: "
        + paymentId, exception.getMessage());
  }
}


