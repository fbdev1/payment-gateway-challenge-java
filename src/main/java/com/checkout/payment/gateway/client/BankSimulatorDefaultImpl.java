package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.AcquiringProcessException;
import com.checkout.payment.gateway.client.model.BankPaymentRequest;
import com.checkout.payment.gateway.client.model.BankPaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.UUID;

@Service
public class BankSimulatorDefaultImpl implements BankSimulatorInterface {

  private static final Logger LOG = LoggerFactory.getLogger(BankSimulatorDefaultImpl.class);

  private final RestTemplate restTemplate;

  @Value("${client.url.default}")
  private String baseUrl;

  public BankSimulatorDefaultImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public PaymentStatus makePayment(UUID paymentId, PostPaymentRequest request) {
    try {
      LOG.info("Making payment request to bank for ID: {}", paymentId);

      String cardNumber = request.getCardNumber();
      String expiryDate = request.getExpiryDate();
      String cvv = request.getCvv();
      String currency = request.getCurrency();
      int amount = request.getAmount();

      BankPaymentRequest bankPaymentRequest = new BankPaymentRequest(
          cardNumber, expiryDate, cvv, currency, amount);

      var response = restTemplate.postForEntity(
          baseUrl + "/payments",
          bankPaymentRequest,
          BankPaymentResponse.class
      );
      BankPaymentResponse body = response.getBody();
      return handleSuccessBankResponse(body, paymentId);

    } catch (AcquiringProcessException ex) {
      throw ex;
    } catch (Exception e) {
      LOG.error(
          "Bank simulator respond with error while making payment request for payment ID: {}, lastFour: {}",
          paymentId, request.getCardNumberLastFour(), e);
      throw new AcquiringProcessException("Bank simulator respond with error while making payment "
          + "request for payment ID: " + paymentId);
    }
  }

  private PaymentStatus handleSuccessBankResponse(BankPaymentResponse response, UUID paymentId) {
    if (response == null) {
      LOG.warn("Bank returned null body for payment ID: {}", paymentId);
      throw new AcquiringProcessException("Bank returned null body for payment ID: " + paymentId);
    }
    if (response.authorized() && response.authorizationCode() != null && !response.authorizationCode().isEmpty()) {
      LOG.info("Bank authorized current operation with payment ID: {} with authorization code: {}",
          paymentId, response.authorizationCode());
      return PaymentStatus.AUTHORIZED;
    }
    if (!response.authorized()) {
      LOG.info("Bank declined current operation with payment ID: {}", paymentId);
      return PaymentStatus.DECLINED;
    }
    LOG.warn("Bank returned empty authorization code for payment ID: {}", paymentId);
    throw new AcquiringProcessException(
        "Bank returned empty authorization code for payment ID: " + paymentId);
  }
}
