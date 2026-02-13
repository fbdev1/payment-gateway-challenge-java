package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.BankSimulatorInterface;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EntityNotFoundException;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final BankSimulatorInterface bankSimulatorClient;

  public PaymentGatewayService(PaymentsRepository paymentsRepository,
      BankSimulatorInterface bankSimulatorClient) {
    this.paymentsRepository = paymentsRepository;
    this.bankSimulatorClient = bankSimulatorClient;
  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.info("Requesting access to the payment with ID {}", id);
    return paymentsRepository.get(id)
        .orElseThrow(() -> {
          LOG.warn("Payment not found. Payment ID: {}", id);
          return new EntityNotFoundException("Payment not found. Payment ID: " + id);
        });
  }

  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    UUID paymentId = UUID.randomUUID();

    LOG.info("Processing payment with payment ID: {}", paymentId);

    PaymentStatus paymentStatus = bankSimulatorClient.makePayment(paymentId, paymentRequest);

    PostPaymentResponse postPaymentResponse = buildResponse(paymentId, paymentRequest,
        paymentStatus);
    if (paymentStatus == PaymentStatus.AUTHORIZED || paymentStatus == PaymentStatus.DECLINED) {
      LOG.info("Payment processed with ID: {}, status: {}", paymentId, paymentStatus);
      paymentsRepository.add(postPaymentResponse);
    }
    return postPaymentResponse;
  }

  private PostPaymentResponse buildResponse(UUID id, PostPaymentRequest request,
      PaymentStatus status) {
    var response = new PostPaymentResponse();
    response.setId(id);
    response.setStatus(status);
    response.setCardNumberLastFour(request.getCardNumberLastFour());
    response.setExpiryMonth(request.getExpiryMonth());
    response.setExpiryYear(request.getExpiryYear());
    response.setCurrency(request.getCurrency());
    response.setAmount(request.getAmount());

    return response;
  }
}
