package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.BankSimulatorInterface;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EntityNotFoundException;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

    @Mock
    private PaymentsRepository paymentsRepository;

    @Mock
    private BankSimulatorInterface bankSimulatorClient;

    @InjectMocks
    private PaymentGatewayService paymentGatewayService;

    @Captor
    private ArgumentCaptor<PostPaymentResponse> paymentResponseCaptor;

    private UUID testPaymentId;
    private PostPaymentRequest testPaymentRequest;
    private PostPaymentResponse existingPaymentResponse;

    @BeforeEach
    void setUp() {
        testPaymentId = UUID.randomUUID();
        testPaymentRequest = createTestPaymentRequest();
        existingPaymentResponse = createTestPaymentResponse();
    }

    @Test
    void getPaymentById_ExistingPayment_ReturnsPaymentResponse() {
        when(paymentsRepository.get(testPaymentId)).thenReturn(Optional.of(existingPaymentResponse));

        PostPaymentResponse result = paymentGatewayService.getPaymentById(testPaymentId);

        assertNotNull(result);
        assertEquals(existingPaymentResponse.getId(), result.getId());
        assertEquals(existingPaymentResponse.getStatus(), result.getStatus());
        assertEquals(existingPaymentResponse.getAmount(), result.getAmount());
        assertEquals(existingPaymentResponse.getCurrency(), result.getCurrency());
        verify(paymentsRepository).get(testPaymentId);
    }

    @Test
    void getPaymentById_NonExistingPayment_ThrowsEntityNotFoundException() {
        when(paymentsRepository.get(testPaymentId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> paymentGatewayService.getPaymentById(testPaymentId));

        assertEquals("Payment not found. Payment ID: " + testPaymentId, exception.getMessage());
        verify(paymentsRepository).get(testPaymentId);
    }

    @Test
    void processPayment_BankAuthorizes_ReturnsAuthorizedResponseAndSavesPayment() {
        when(bankSimulatorClient.makePayment(any(UUID.class), eq(testPaymentRequest)))
            .thenReturn(PaymentStatus.AUTHORIZED);

        PostPaymentResponse result = paymentGatewayService.processPayment(testPaymentRequest);

        assertNotNull(result);
        assertEquals(PaymentStatus.AUTHORIZED, result.getStatus());
        assertEquals(testPaymentRequest.getAmount(), result.getAmount());
        assertEquals(testPaymentRequest.getCurrency(), result.getCurrency());
        assertEquals(testPaymentRequest.getExpiryMonth(), result.getExpiryMonth());
        assertEquals(testPaymentRequest.getExpiryYear(), result.getExpiryYear());
        assertEquals(testPaymentRequest.getCardNumberLastFour(), result.getCardNumberLastFour());
        assertNotNull(result.getId());

        verify(bankSimulatorClient).makePayment(any(UUID.class), eq(testPaymentRequest));
        verify(paymentsRepository).add(paymentResponseCaptor.capture());
        
        PostPaymentResponse savedPayment = paymentResponseCaptor.getValue();
        assertEquals(result.getId(), savedPayment.getId());
        assertEquals(PaymentStatus.AUTHORIZED, savedPayment.getStatus());
    }

    @Test
    void processPayment_BankDeclines_ReturnsDeclinedResponseAndSavesPayment() {
        when(bankSimulatorClient.makePayment(any(UUID.class), eq(testPaymentRequest)))
            .thenReturn(PaymentStatus.DECLINED);

        PostPaymentResponse result = paymentGatewayService.processPayment(testPaymentRequest);

        assertNotNull(result);
        assertEquals(PaymentStatus.DECLINED, result.getStatus());
        assertEquals(testPaymentRequest.getAmount(), result.getAmount());
        assertEquals(testPaymentRequest.getCurrency(), result.getCurrency());
        assertEquals(testPaymentRequest.getExpiryMonth(), result.getExpiryMonth());
        assertEquals(testPaymentRequest.getExpiryYear(), result.getExpiryYear());
        assertEquals(testPaymentRequest.getCardNumberLastFour(), result.getCardNumberLastFour());
        assertNotNull(result.getId());

        verify(bankSimulatorClient).makePayment(any(UUID.class), eq(testPaymentRequest));
        verify(paymentsRepository).add(paymentResponseCaptor.capture());
        
        PostPaymentResponse savedPayment = paymentResponseCaptor.getValue();
        assertEquals(result.getId(), savedPayment.getId());
        assertEquals(PaymentStatus.DECLINED, savedPayment.getStatus());
    }

    @Test
    void processPayment_BankReturnsOtherStatus_ReturnsResponseAndDoesNotSavePayment() {
        when(bankSimulatorClient.makePayment(any(UUID.class), eq(testPaymentRequest)))
            .thenReturn(PaymentStatus.REJECTED);

        PostPaymentResponse result = paymentGatewayService.processPayment(testPaymentRequest);

        assertNotNull(result);
        assertEquals(PaymentStatus.REJECTED, result.getStatus());
        assertEquals(testPaymentRequest.getAmount(), result.getAmount());
        assertEquals(testPaymentRequest.getCurrency(), result.getCurrency());
        assertEquals(testPaymentRequest.getExpiryMonth(), result.getExpiryMonth());
        assertEquals(testPaymentRequest.getExpiryYear(), result.getExpiryYear());
        assertEquals(testPaymentRequest.getCardNumberLastFour(), result.getCardNumberLastFour());
        assertNotNull(result.getId());

        verify(bankSimulatorClient).makePayment(any(UUID.class), eq(testPaymentRequest));
        verify(paymentsRepository, never()).add(any(PostPaymentResponse.class));
    }

    @Test
    void processPayment_WithValidCardNumber_ReturnsResponseWithCorrectLastFour() {
        testPaymentRequest.setCardNumber("4532015112830366");
        when(bankSimulatorClient.makePayment(any(UUID.class), eq(testPaymentRequest)))
            .thenReturn(PaymentStatus.AUTHORIZED);

        PostPaymentResponse result = paymentGatewayService.processPayment(testPaymentRequest);

        assertNotNull(result);
        assertEquals("0366", result.getCardNumberLastFour());
        verify(bankSimulatorClient).makePayment(any(UUID.class), eq(testPaymentRequest));
        verify(paymentsRepository).add(any(PostPaymentResponse.class));
    }

    @Test
    void processPayment_BankSimulatorThrowsException_PropagatesException() {
        RuntimeException expectedException = new RuntimeException("Bank service unavailable");
        when(bankSimulatorClient.makePayment(any(UUID.class), eq(testPaymentRequest)))
            .thenThrow(expectedException);

        RuntimeException actualException = assertThrows(RuntimeException.class,
            () -> paymentGatewayService.processPayment(testPaymentRequest));

        assertEquals("Bank service unavailable", actualException.getMessage());
        verify(bankSimulatorClient).makePayment(any(UUID.class), eq(testPaymentRequest));
        verify(paymentsRepository, never()).add(any(PostPaymentResponse.class));
    }

    private PostPaymentRequest createTestPaymentRequest() {
        PostPaymentRequest request = new PostPaymentRequest();
        request.setCardNumber("4532015112830366");
        request.setExpiryMonth(12);
        request.setExpiryYear(2025);
        request.setCurrency("USD");
        request.setAmount(10000);
        request.setCvv("123");
        return request;
    }

    private PostPaymentResponse createTestPaymentResponse() {
        PostPaymentResponse response = new PostPaymentResponse();
        response.setId(testPaymentId);
        response.setStatus(PaymentStatus.AUTHORIZED);
        response.setAmount(10000);
        response.setCurrency("USD");
        response.setExpiryMonth(12);
        response.setExpiryYear(2025);
        response.setCardNumberLastFour("3366");
        return response;
    }
}
