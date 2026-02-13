package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import java.util.UUID;

public interface BankSimulatorInterface {

  PaymentStatus makePayment(UUID paymentId, PostPaymentRequest request);
}
